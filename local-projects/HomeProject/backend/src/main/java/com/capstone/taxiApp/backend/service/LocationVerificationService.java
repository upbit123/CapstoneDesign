package com.capstone.taxiApp.backend.service;

import com.capstone.taxiApp.backend.dto.VerificationPolicyResponse;
import com.capstone.taxiApp.backend.dto.VerifyHostDeviceLocationRequest;
import com.capstone.taxiApp.backend.dto.VerifyLocationRequest;
import com.capstone.taxiApp.backend.dto.VerifyLocationResponse;
import com.capstone.taxiApp.backend.entity.LocationVerificationPolicy;
import com.capstone.taxiApp.backend.entity.LocationVerificationRecord;
import com.capstone.taxiApp.backend.entity.University;
import com.capstone.taxiApp.backend.repository.LocationVerificationPolicyRepository;
import com.capstone.taxiApp.backend.repository.LocationVerificationRecordRepository;
import com.capstone.taxiApp.backend.repository.UniversityRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.time.LocalDateTime;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// 위치 인증 핵심 비즈니스 로직 서비스.
// 앱이 보낸 GPS 좌표를 (universities 캠퍼스 경계 + service_zones 세부 구역) 두 레이어와 비교해
// 인증 성공/실패를 판정하고 결과를 location_verifications 테이블에 저장한다.
@Service
public class LocationVerificationService {

    private static final String STATUS_APPROVED = "APPROVED";            // 인증 성공
    private static final String STATUS_OUT_OF_RANGE = "OUT_OF_RANGE";    // 허용 구역 외부
    private static final String STATUS_LOW_ACCURACY = "LOW_ACCURACY";    // GPS 정확도 부족
    private static final int VERIFICATION_EXPIRE_MINUTES = 10;           // 인증 유효 시간(분)
    private static final double DEFAULT_MAX_ACCURACY_METERS = 60d;       // GPS 허용 오차 상한(미터)
    private static final long CAMPUS_ZONE_ID = 0L;                       // 캠퍼스 경계 구역의 가상 zoneId
    private static final String PURPOSE_LOCATION_LOAD_TEST = "LOCATION_LOAD_TEST"; // 구역 데이터 없을 때도 저장만 허용하는 테스트 목적 코드
    private static final double HOST_LOCATION_ACCURACY_METERS = 3000d;   // IP 기반 위치의 예상 오차(3km)
    // ipapi.co JSON 응답에서 위도/경도를 추출하는 정규식
    private static final Pattern LATITUDE_PATTERN = Pattern.compile("\\\"latitude\\\"\\s*:\\s*(-?\\d+(?:\\.\\d+)?)");
    private static final Pattern LONGITUDE_PATTERN = Pattern.compile("\\\"longitude\\\"\\s*:\\s*(-?\\d+(?:\\.\\d+)?)");

    private final LocationVerificationPolicyRepository policyRepository;         // service_zones 테이블
    private final LocationVerificationRecordRepository verificationRecordRepository; // location_verifications 테이블
    private final UniversityRepository universityRepository;                      // universities 테이블

    // 생성자 주입. 세 개의 리포지토리를 받아 인증 파이프라인 전체를 처리한다.
    public LocationVerificationService(
            LocationVerificationPolicyRepository policyRepository,
            LocationVerificationRecordRepository verificationRecordRepository,
            UniversityRepository universityRepository
    ) {
        this.policyRepository = policyRepository;
        this.verificationRecordRepository = verificationRecordRepository;
        this.universityRepository = universityRepository;
    }

    // GET /policies/{universityId} 에 응답하는 메서드.
    // universities 캠퍼스 경계와 service_zones 세부 구역을 합산한 전체 정책 목록을 반환한다.
    // 앱은 이 정책 목록을 지도에 원형으로 그리고, 로컬 폴백 판정에도 재사용한다.
    public List<VerificationPolicyResponse> getActivePolicies(Long universityId) {
        List<EffectivePolicy> policies = buildEffectivePolicies(universityId);

        // 내부 EffectivePolicy 객체를 앱이 이해할 수 있는 응답 DTO로 변환한다.
        return policies.stream()
                .map(policy -> new VerificationPolicyResponse(
                        policy.policyId,
                        policy.universityId,
                        policy.zoneId,
                        policy.policyName,
                        DEFAULT_MAX_ACCURACY_METERS,
                        policy.centerLatitude,
                        policy.centerLongitude,
                        policy.allowedRadiusMeters
                ))
                .toList();
    }

    // POST /verify 에 응답하는 핵심 인증 메서드.
    // 앱 단말 GPS 좌표를 N개 원형 구역과 순서대로 비교해 첫 번째 포함 구역을 찾는다.
    // 결과(성공/실패/정확도부족)와 매칭 구역 정보를 DB에 저장한 뒤 응답으로 반환한다.
    @Transactional
    public VerifyLocationResponse verify(VerifyLocationRequest request) {
        // 인증에 사용할 정책 목록 구성 (캠퍼스 경계 + 서비스 존)
        List<EffectivePolicy> policies = buildEffectivePolicies(request.universityId());
        // LOCATION_LOAD_TEST 목적 코드는 정책 없이도 좌표만 저장 가능하도록 허용한다.
        boolean isLocationLoadTest = PURPOSE_LOCATION_LOAD_TEST.equalsIgnoreCase(request.requestPurpose());

        if (policies.isEmpty() && !isLocationLoadTest) {
            throw new IllegalArgumentException("활성화된 위치 인증 정책이 없습니다.");
        }

        EffectivePolicy matchedPolicy = null;
        double nearestDistance = Double.MAX_VALUE; // 포함 구역이 없을 때 가장 가까운 거리를 기록해 응답에 제공한다.
        double matchedDistance = Double.MAX_VALUE;
        // 정책 목록을 순서대로 순회하며 첫 번째 포함 구역에서 탐색을 중단한다.
        for (EffectivePolicy policy : policies) {
            double distance = distanceMeters(
                    request.latitude(),
                    request.longitude(),
                    policy.centerLatitude,
                    policy.centerLongitude
            );

            // 포함 구역이 없을 경우를 대비해 최근접 거리를 누적 갱신한다.
            if (distance < nearestDistance) {
                nearestDistance = distance;
            }

            // 사용자 좌표가 원형 구역 반경 이내이면 매칭 성공으로 처리한다.
            if (distance <= policy.allowedRadiusMeters) {
                matchedPolicy = policy;
                matchedDistance = distance;
                break;
            }
        }

        String status;
        String failureReason = null;
        LocalDateTime expiresAt = null;

        // 구역 외부 → 정확도 부족 → 승인 순서로 상태를 판정한다.
        // 정확도 검사는 구역 포함이 확인된 이후에만 수행한다.
        if (matchedPolicy == null) {
            // 어떤 원형 구역에도 포함되지 않음
            status = STATUS_OUT_OF_RANGE;
            failureReason = policies.isEmpty()
                    ? "인증 구역 데이터 없음: 현재 위치만 저장됨"
                    : "허용 원형 구역 외부";
        } else if (request.accuracyMeters() > DEFAULT_MAX_ACCURACY_METERS) {
            // 구역 안에는 있지만 GPS 오차가 60m를 초과해 위치 신뢰도가 낮음
            status = STATUS_LOW_ACCURACY;
            failureReason = "허용 정확도 초과";
        } else {
            // 구역 포함 + 정확도 양호 → 인증 성공
            status = STATUS_APPROVED;
            // 인증 유효 만료 시각을 10분 뒤로 설정한다.
            expiresAt = LocalDateTime.now().plusMinutes(VERIFICATION_EXPIRE_MINUTES);
        }
        LocalDateTime verifiedAt = LocalDateTime.now();
        // 응답용 거리: 매칭 성공이면 매칭 구역까지, 실패이면 가장 가까운 구역까지의 거리를 제공한다.
        double distanceForResponse = matchedPolicy != null ? matchedDistance : nearestDistance;

        // 판정 결과를 location_verifications 테이블에 저장한다.
        LocationVerificationRecord verificationRecord = new LocationVerificationRecord();
        verificationRecord.setUserId(request.userId());
        verificationRecord.setUniversityId(request.universityId());
        verificationRecord.setRequestPurpose(request.requestPurpose());
        verificationRecord.setLatitude(request.latitude());               // 앱이 보낸 원본 GPS 위도
        verificationRecord.setLongitude(request.longitude());             // 앱이 보낸 원본 GPS 경도
        verificationRecord.setAccuracyMeters(request.accuracyMeters());   // 단말 GPS 정확도(미터)
        verificationRecord.setCapturedAtEpochMillis(request.capturedAtEpochMillis()); // 단말 GPS 수집 시각
        verificationRecord.setVerificationStatus(status);                 // APPROVED / OUT_OF_RANGE / LOW_ACCURACY
        verificationRecord.setApproved(STATUS_APPROVED.equals(status) ? 1 : 0); // Oracle BOOLEAN 대신 NUMBER 사용
        verificationRecord.setMatchedPolicyId(matchedPolicy != null ? matchedPolicy.policyId : null);
        verificationRecord.setMatchedZoneId(matchedPolicy != null ? matchedPolicy.zoneId : null);
        verificationRecord.setMatchedZoneName(matchedPolicy != null ? matchedPolicy.policyName : null);
        verificationRecord.setCheckedPolicyCount(policies.size());        // 이번 인증에서 검사한 정책 수
        verificationRecord.setDistanceToCenterMeters(distanceForResponse);
        verificationRecord.setFailureReason(failureReason);               // 성공이면 null
        verificationRecord.setVerifiedAt(verifiedAt);
        verificationRecord.setExpiresAt(expiresAt);                       // 성공이면 verifiedAt+10분, 실패이면 null

        LocationVerificationRecord savedRecord = verificationRecordRepository.save(verificationRecord);

        // 저장된 레코드의 PK(verificationId)와 저장된 좌표를 포함해 응답을 구성한다.
        return new VerifyLocationResponse(
                savedRecord.getVerificationId(),
                STATUS_APPROVED.equals(status),
                status,
            savedRecord.getLatitude(),   // DB에 저장된 위도(앱이 지도 마커를 그릴 때 사용)
            savedRecord.getLongitude(),  // DB에 저장된 경도
                matchedPolicy != null ? matchedPolicy.policyId : null,
                matchedPolicy != null ? matchedPolicy.zoneId : null,
                matchedPolicy != null ? matchedPolicy.policyName : null,
                policies.size(),
                distanceForResponse,
                failureReason,
                verifiedAt,
                expiresAt
        );
    }

    // /verify-host-device 엔드포인트에 응답하는 메서드.
    // 앱 좌표 대신 ipapi.co API로 서버(노트북) 현재 IP 위치를 조회해 verify()를 호출한다.
    // 개발/테스트 목적으로 에뮬레이터 Location 설정 없이 위치를 빠르게 저장할 때 사용한다.
    // IP 기반 위치의 정확도는 약 3km이므로 상용 인증에는 적합하지 않다.
    @Transactional
    public VerifyLocationResponse verifyFromHostDevice(VerifyHostDeviceLocationRequest request) {
        // ipapi.co에서 호스트 IP 기반 위도/경도를 조회한다.
        HostDeviceLocation hostDeviceLocation = resolveHostDeviceLocation();
        // 조회한 좌표를 GPS 요청 형식으로 변환해 기존 verify() 로직을 재사용한다.
        VerifyLocationRequest verifyLocationRequest = new VerifyLocationRequest(
                request.userId(),
                request.universityId(),
                // requestPurpose가 null이면 테스트 코드로 처리해 정책 없이도 저장 가능하게 한다.
                request.requestPurpose() != null ? request.requestPurpose() : PURPOSE_LOCATION_LOAD_TEST,
                hostDeviceLocation.latitude,
                hostDeviceLocation.longitude,
                HOST_LOCATION_ACCURACY_METERS, // IP 위치 오차 3000m를 정확도 값으로 사용한다.
                System.currentTimeMillis()     // 서버 처리 시각을 캡처 시각으로 기록한다.
        );
        return verify(verifyLocationRequest);
    }

    // 인증에 사용할 최종 정책 목록을 구성하는 내부 메서드.
    // 1순위: universities 테이블의 캠퍼스 전체 경계 원형 (boundary_radius_m이 있을 때만 추가)
    // 2순위: service_zones 테이블의 개별 탑승/하차 구역 (is_active=1 인 것만, zoneId 오름차순)
    // 두 레이어를 합친 목록을 순서대로 탐색해 첫 번째 포함 구역을 인증 결과로 사용한다.
    private List<EffectivePolicy> buildEffectivePolicies(Long universityId) {
            // 학교가 없으면 즉시 예외 발생 → ApiExceptionHandler가 400으로 변환한다.
            University university = universityRepository.findById(universityId)
                .orElseThrow(() -> new IllegalArgumentException("UNIVERSITIES 테이블에서 학교를 찾을 수 없습니다."));

            List<EffectivePolicy> effectivePolicies = new ArrayList<>();

            // 캠퍼스 경계가 설정된 경우에만 최상위 원형 구역으로 추가한다.
            // zoneId는 CAMPUS_ZONE_ID(0)으로 고정해 서비스 존과 구분한다.
            if (university.getLatitude() != null
                && university.getLongitude() != null
                && university.getBoundaryRadiusMeters() != null
                && university.getBoundaryRadiusMeters().doubleValue() > 0d) {
                effectivePolicies.add(new EffectivePolicy(
                    university.getUniversityId(),
                    university.getUniversityId(),
                    CAMPUS_ZONE_ID,
                    university.getUniversityName() + " 캠퍼스",
                    university.getLatitude().doubleValue(),
                    university.getLongitude().doubleValue(),
                    university.getBoundaryRadiusMeters().doubleValue()
                ));
            }

            // service_zones에서 해당 학교의 활성 구역(is_active=1)을 zoneId 순으로 가져온다.
            List<LocationVerificationPolicy> zonePolicies = policyRepository
                .findByUniversityIdAndIsActiveOrderByZoneIdAsc(universityId, 1);
            for (LocationVerificationPolicy zonePolicy : zonePolicies) {
                effectivePolicies.add(new EffectivePolicy(
                    zonePolicy.getPolicyId(),
                    zonePolicy.getUniversityId(),
                    zonePolicy.getZoneId(),
                    zonePolicy.getPolicyName(),
                    zonePolicy.getCenterLatitude().doubleValue(),
                    zonePolicy.getCenterLongitude().doubleValue(),
                    zonePolicy.getAllowedRadiusM().doubleValue()
                ));
            }

            return effectivePolicies;
    }

    // 두 GPS 좌표 사이의 구면 거리를 Haversine 공식으로 계산한다.
    // 지구를 구(球)로 가정하며 약 0.5% 이내의 오차가 있다. 수백 미터 단위 인증에는 충분히 정확하다.
    // lat1/lng1: 사용자 좌표, lat2/lng2: 구역 중심 좌표
    private double distanceMeters(double lat1, double lng1, double lat2, double lng2) {
        final double earthRadiusMeters = 6371000d; // 지구 평균 반지름(미터)
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);

        double sinLat = Math.sin(dLat / 2d);
        double sinLng = Math.sin(dLng / 2d);
        // Haversine 공식: a = sin²(Δlat/2) + cos(lat1)·cos(lat2)·sin²(Δlng/2)
        double a = sinLat * sinLat
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * sinLng * sinLng;
        // c = 2·atan2(√a, √(1−a))  →  거리 = R·c
        double c = 2d * Math.atan2(Math.sqrt(a), Math.sqrt(1d - a));
        return earthRadiusMeters * c;
    }

    // ipapi.co 서비스에 HTTP GET 요청을 보내 현재 서버(노트북)의 IP 기반 위치를 조회한다.
    // 타임아웃 5초, 실패 시 IllegalStateException을 던져 호출부에서 에러 응답으로 처리한다.
    // 참고: IP 위치는 건물/동 수준이 아니라 도시 수준(약 3km 오차)이다.
    private HostDeviceLocation resolveHostDeviceLocation() {
        HttpURLConnection connection = null;
        try {
            // ipapi.co는 요청 IP의 지리 정보를 JSON으로 반환한다. (무료 플랜: 분당 30회 제한)
            URL url = new URL("https://ipapi.co/json/");
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);

            int responseCode = connection.getResponseCode();
            InputStream stream = responseCode >= 200 && responseCode < 300
                    ? connection.getInputStream()
                    : connection.getErrorStream();
            String body = readStream(stream);
            if (responseCode < 200 || responseCode >= 300) {
                throw new IllegalStateException("호스트 위치 조회 실패 (HTTP " + responseCode + ")");
            }

            // JSON 파싱 라이브러리 없이 정규식으로 위도/경도 값만 추출한다.
            Matcher latitudeMatcher = LATITUDE_PATTERN.matcher(body);
            Matcher longitudeMatcher = LONGITUDE_PATTERN.matcher(body);
            if (!latitudeMatcher.find() || !longitudeMatcher.find()) {
                throw new IllegalStateException("호스트 위치 응답에 latitude/longitude가 없습니다.");
            }

            return new HostDeviceLocation(
                    Double.parseDouble(latitudeMatcher.group(1)),
                    Double.parseDouble(longitudeMatcher.group(1))
            );
        } catch (Exception e) {
            throw new IllegalStateException("호스트 위치를 가져오지 못했습니다.", e);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private String readStream(InputStream stream) throws Exception {
        if (stream == null) {
            return "";
        }
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            StringBuilder builder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }
            return builder.toString();
        }
    }

    // universities와 service_zones를 통합해 인증 계산에 사용하는 단순 값 객체(VO).
    // DB 엔티티 타입을 직접 넘기지 않고 이 객체로 정규화해 인증 로직의 DB 의존성을 낮춘다.
    private static final class EffectivePolicy {
        private final Long policyId;             // 정책 식별자 (캠퍼스: universityId, 서비스존: zoneId)
        private final Long universityId;         // 소속 학교 ID
        private final Long zoneId;               // 서비스 존 ID (캠퍼스 경계는 CAMPUS_ZONE_ID=0)
        private final String policyName;         // 구역 표시 이름
        private final double centerLatitude;     // 원형 중심 위도
        private final double centerLongitude;    // 원형 중심 경도
        private final double allowedRadiusMeters; // 인증 허용 반경(미터)

        private EffectivePolicy(
                Long policyId,
                Long universityId,
                Long zoneId,
                String policyName,
                double centerLatitude,
                double centerLongitude,
                double allowedRadiusMeters
        ) {
            this.policyId = policyId;
            this.universityId = universityId;
            this.zoneId = zoneId;
            this.policyName = policyName;
            this.centerLatitude = centerLatitude;
            this.centerLongitude = centerLongitude;
            this.allowedRadiusMeters = allowedRadiusMeters;
        }
    }

    // ipapi.co 응답에서 추출한 호스트 IP 위치를 담는 단순 값 객체.
    private static final class HostDeviceLocation {
        private final double latitude;  // IP 기반 위도
        private final double longitude; // IP 기반 경도

        private HostDeviceLocation(double latitude, double longitude) {
            this.latitude = latitude;
            this.longitude = longitude;
        }
    }
}

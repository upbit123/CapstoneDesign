package com.capstone.taxiApp.backend.dto;

import java.time.LocalDateTime;

// /verify 및 /verify-host-device 엔드포인트가 앱으로 반환하는 인증 결과 응답 모델.
// 앱은 approved 값으로 인증 성공 여부를 판단하고, 나머지 필드는 화면 표시와 진단에 사용한다.
public record VerifyLocationResponse(
        Long verificationId,            // DB에 저장된 인증 이력 ID (앱 화면에 표시됨)
        boolean approved,               // 인증 승인 여부 (true: 성공, false: 실패)
        String verificationStatus,      // 상태 코드: APPROVED / OUT_OF_RANGE / LOW_ACCURACY
        Double savedLatitude,           // DB에 저장된 위도 (앱 지도 마커 표시에 사용)
        Double savedLongitude,          // DB에 저장된 경도
        Long matchedPolicyId,           // 인증된 정책 ID (실패 시 null)
        Long matchedZoneId,             // 인증된 서비스 존 ID (실패 시 null)
        String matchedZoneName,         // 인증된 구역 이름 (실패 시 null)
        int checkedPolicyCount,         // 이번 인증에서 비교한 전체 구역 수
        Double distanceToCenterMeters,  // 가장 가까운 구역 중심점까지의 거리(미터)
        String failureReason,           // 실패 사유 (성공 시 null)
        LocalDateTime verifiedAt,       // 서버 인증 처리 시각
        LocalDateTime expiresAt         // 인증 유효 만료 시각 (성공 시 verifiedAt + 10분, 실패 시 null)
) {
}

package com.capstone.taxiApp.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

// LOCATION_VERIFICATIONS 테이블을 매핑하는 이력 엔티티.
// 위치 인증 버튼을 누를 때마다 한 행이 삽입되며, 인증 성공/실패 내역을 모두 보존한다.
// 이 이력을 통해 특정 사용자가 특정 시각에 어느 구역에 있었는지 추적할 수 있다.
@Entity
@Table(name = "location_verifications")
public class LocationVerificationRecord {

    // 인증 이력 고유 식별자 (PK, Oracle IDENTITY). 앱 화면에 "인증 이력 ID"로 표시된다.
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "verification_id")
    private Long verificationId;

    // 인증을 요청한 사용자 ID (USERS 테이블 참조)
    @Column(name = "user_id", nullable = false)
    private Long userId;

    // 인증 대상 학교 ID (UNIVERSITIES 테이블 참조)
    @Column(name = "university_id", nullable = false)
    private Long universityId;

    // 인증 요청 목적 코드 (예: GPS_PRECISION_VERIFY, LOCATION_LOAD_TEST)
    @Column(name = "request_purpose")
    private String requestPurpose;

    // 앱 단말이 보고한 사용자 위도
    @Column(name = "latitude", nullable = false)
    private Double latitude;

    // 앱 단말이 보고한 사용자 경도
    @Column(name = "longitude", nullable = false)
    private Double longitude;

    // 단말 GPS 정확도(미터). 값이 클수록 부정확한 위치. NUMBER(10,2) 타입으로 Oracle 스키마와 맞춤.
    @Column(name = "accuracy_m", nullable = false, columnDefinition = "NUMBER(10,2)")
    private Double accuracyMeters;

    // 단말에서 GPS를 수집한 시각 (Unix 에폭 밀리초). 위치 신선도 검증에 활용.
    @Column(name = "captured_at_epoch_millis")
    private Long capturedAtEpochMillis;

    // 인증 결과 상태 코드: APPROVED(성공) / OUT_OF_RANGE(구역 밖) / LOW_ACCURACY(정확도 부족)
    @Column(name = "verification_status", nullable = false)
    private String verificationStatus;

    // 인증 승인 여부: 1=승인, 0=거부. boolean 대신 정수형을 사용하는 이유는 Oracle NUMBER 타입 호환.
    @Column(name = "approved", nullable = false)
    private Integer approved;

    // 매칭된 정책(서비스 존) ID. 인증 실패 시 null.
    @Column(name = "matched_policy_id")
    private Long matchedPolicyId;

    // 매칭된 서비스 존 ID (SERVICE_ZONES 테이블 참조). 인증 실패 시 null.
    @Column(name = "matched_zone_id")
    private Long matchedZoneId;

    // 매칭된 구역 이름 (예: "정문 앞"). 이력 조회 시 join 없이 확인 가능하도록 비정규화 저장.
    @Column(name = "matched_zone_name")
    private String matchedZoneName;

    // 이 인증 요청 시 비교한 전체 정책(구역) 개수. 0이면 활성 정책이 없어 위치만 저장된 케이스.
    @Column(name = "checked_policy_count", nullable = false)
    private Integer checkedPolicyCount;

    // 가장 가까운 구역 중심점까지의 거리(미터). 인증 실패 시에도 얼마나 벗어났는지 확인 가능.
    @Column(name = "distance_to_center_m")
    private Double distanceToCenterMeters;

    // 인증 실패 사유 (예: "허용 원형 구역 외부", "허용 정확도 초과"). 성공 시 null.
    @Column(name = "failure_reason")
    private String failureReason;

    // 인증이 서버에서 처리된 시각 (서버 시간 기준)
    @Column(name = "verified_at", nullable = false)
    private LocalDateTime verifiedAt;

    // 인증 유효 만료 시각. 성공 시 현재 시각 + 10분. 실패 시 null.
    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    public Long getVerificationId() {
        return verificationId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getUniversityId() {
        return universityId;
    }

    public void setUniversityId(Long universityId) {
        this.universityId = universityId;
    }

    public String getRequestPurpose() {
        return requestPurpose;
    }

    public void setRequestPurpose(String requestPurpose) {
        this.requestPurpose = requestPurpose;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Double getAccuracyMeters() {
        return accuracyMeters;
    }

    public void setAccuracyMeters(Double accuracyMeters) {
        this.accuracyMeters = accuracyMeters;
    }

    public Long getCapturedAtEpochMillis() {
        return capturedAtEpochMillis;
    }

    public void setCapturedAtEpochMillis(Long capturedAtEpochMillis) {
        this.capturedAtEpochMillis = capturedAtEpochMillis;
    }

    public String getVerificationStatus() {
        return verificationStatus;
    }

    public void setVerificationStatus(String verificationStatus) {
        this.verificationStatus = verificationStatus;
    }

    public Integer getApproved() {
        return approved;
    }

    public void setApproved(Integer approved) {
        this.approved = approved;
    }

    public Long getMatchedPolicyId() {
        return matchedPolicyId;
    }

    public void setMatchedPolicyId(Long matchedPolicyId) {
        this.matchedPolicyId = matchedPolicyId;
    }

    public Long getMatchedZoneId() {
        return matchedZoneId;
    }

    public void setMatchedZoneId(Long matchedZoneId) {
        this.matchedZoneId = matchedZoneId;
    }

    public String getMatchedZoneName() {
        return matchedZoneName;
    }

    public void setMatchedZoneName(String matchedZoneName) {
        this.matchedZoneName = matchedZoneName;
    }

    public Integer getCheckedPolicyCount() {
        return checkedPolicyCount;
    }

    public void setCheckedPolicyCount(Integer checkedPolicyCount) {
        this.checkedPolicyCount = checkedPolicyCount;
    }

    public Double getDistanceToCenterMeters() {
        return distanceToCenterMeters;
    }

    public void setDistanceToCenterMeters(Double distanceToCenterMeters) {
        this.distanceToCenterMeters = distanceToCenterMeters;
    }

    public String getFailureReason() {
        return failureReason;
    }

    public void setFailureReason(String failureReason) {
        this.failureReason = failureReason;
    }

    public LocalDateTime getVerifiedAt() {
        return verifiedAt;
    }

    public void setVerifiedAt(LocalDateTime verifiedAt) {
        this.verifiedAt = verifiedAt;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }
}
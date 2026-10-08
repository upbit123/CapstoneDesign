package com.capstone.taxiApp.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

import java.math.BigDecimal;

// SERVICE_ZONES 테이블을 매핑하는 엔티티.
// 학교 캠퍼스 내 세부 택시 탑승 구역을 원형(중심 좌표 + 반경)으로 정의한다.
// LocationVerificationService에서 캠퍼스 경계(universities)와 조합해 최종 인증 구역을 구성한다.
@Entity
@Table(name = "service_zones")
public class LocationVerificationPolicy {

    // 서비스 존 고유 식별자 (PK, auto increment)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "zone_id")
    private Long zoneId;

    // 이 구역이 속한 학교 ID (universities.university_id 참조)
    @Column(name = "university_id", nullable = false)
    private Long universityId;

    // 구역 이름 (화면 표시 및 인증 결과에 사용됨. 예: "정문 앞")
    @Column(name = "zone_name", nullable = false)
    private String policyName;

    // 구역 유형 (예: PICKUP, DROPOFF 등 운영 목적 분류)
    @Column(name = "zone_type", nullable = false)
    private String verificationType;

    // 원형 구역 중심의 위도
    @Column(name = "latitude", nullable = false)
    private BigDecimal centerLatitude;

    // 원형 구역 중심의 경도
    @Column(name = "longitude", nullable = false)
    private BigDecimal centerLongitude;

    // 인증 허용 반경(미터). 사용자 좌표가 이 반경 이내일 때 해당 구역으로 인증됨.
    @Column(name = "radius_m", nullable = false)
    private BigDecimal allowedRadiusM;

    // DB에 별도 컬럼이 없으므로 기본 허용 GPS 정확도(60m)를 코드 상수로 고정해 사용한다.
    // 실제 운영 시 DB 컬럼으로 분리해 구역별로 다르게 설정할 수 있다.
    @Transient
    private final BigDecimal maxAccuracyM = BigDecimal.valueOf(60);

    // 0: 비활성(무시), 1: 활성(인증 구역으로 사용). DB에서 직접 ON/OFF 제어 가능.
    @Column(name = "is_active", nullable = false)
    private Integer isActive;

    public Long getPolicyId() {
        return zoneId;
    }

    public Long getUniversityId() {
        return universityId;
    }

    public Long getZoneId() {
        return zoneId;
    }

    public String getPolicyName() {
        return policyName;
    }

    public String getVerificationType() {
        return verificationType;
    }

    public BigDecimal getCenterLatitude() {
        return centerLatitude;
    }

    public BigDecimal getCenterLongitude() {
        return centerLongitude;
    }

    public BigDecimal getAllowedRadiusM() {
        return allowedRadiusM;
    }

    public BigDecimal getMaxAccuracyM() {
        return maxAccuracyM;
    }

    public Integer getIsActive() {
        return isActive;
    }
}

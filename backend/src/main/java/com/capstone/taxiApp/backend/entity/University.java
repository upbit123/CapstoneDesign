package com.capstone.taxiApp.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;

// UNIVERSITIES 테이블을 매핑하는 엔티티.
// 학교 캠퍼스 중심 좌표와 반경을 보유하며, 위치 인증 정책의 최상위 구역으로 사용된다.
@Entity
@Table(name = "universities")
public class University {

    // 학교 고유 식별자 (PK, auto increment)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "university_id")
    private Long universityId;

    // 학교 이름 (예: "인천대학교")
    @Column(name = "university_name", nullable = false)
    private String universityName;

    @Column(name = "school_email_domain", nullable = false)
    private String schoolEmailDomain;

    // 캠퍼스 대표 위도 좌표
    @Column(name = "latitude")
    private BigDecimal latitude;

    // 캠퍼스 대표 경도 좌표
    @Column(name = "longitude")
    private BigDecimal longitude;

    // 캠퍼스 인증 반경(미터). 이 반경 안에 있으면 캠퍼스 구역 인증 성공으로 처리된다.
    @Column(name = "boundary_radius_m")
    private BigDecimal boundaryRadiusMeters;

    public Long getUniversityId() {
        return universityId;
    }

    public String getUniversityName() {
        return universityName;
    }

    public String getSchoolEmailDomain() {
        return schoolEmailDomain;
    }

    public BigDecimal getLatitude() {
        return latitude;
    }

    public BigDecimal getLongitude() {
        return longitude;
    }

    public BigDecimal getBoundaryRadiusMeters() {
        return boundaryRadiusMeters;
    }
}

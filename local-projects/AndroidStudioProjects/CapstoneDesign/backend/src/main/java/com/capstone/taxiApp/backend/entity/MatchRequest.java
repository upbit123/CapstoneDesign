package com.capstone.taxiApp.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

// MATCH_REQUESTS 테이블을 매핑하는 엔티티.
// 사용자가 특정 출발/도착 구역으로 합승 요청을 생성하면 한 행이 삽입된다.
@Entity
@Table(name = "match_requests")
public class MatchRequest {

    // 합승 요청 고유 식별자 (PK, auto increment)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "request_id")
    private Long requestId;

    // 요청을 생성한 사용자 ID
    @Column(name = "user_id", nullable = false)
    private Long userId;

    // 요청이 속한 학교 ID
    @Column(name = "university_id", nullable = false)
    private Long universityId;

    // 출발 서비스 존 ID (SERVICE_ZONES 참조)
    @Column(name = "start_zone_id", nullable = false)
    private Long startZoneId;

    // 목적지 서비스 존 ID (SERVICE_ZONES 참조)
    @Column(name = "target_zone_id", nullable = false)
    private Long targetZoneId;

    // 이동 방향 유형 (예: TO_CAMPUS, FROM_CAMPUS)
    @Column(name = "direction_type", nullable = false)
    private String directionType;

    // 요청 상태: WAITING(대기 중), MATCHED(매칭 완료), CANCELLED(취소)
    @Column(name = "request_status", nullable = false)
    private String requestStatus;

    // 희망 출발 시각
    @Column(name = "desired_departure_at", nullable = false)
    private LocalDateTime desiredDepartureAt;

    // 최대 대기 허용 시간(분)
    @Column(name = "max_wait_minutes", nullable = false)
    private Integer maxWaitMinutes;

    // 요청이 서버에서 접수된 시각
    @Column(name = "requested_at", nullable = false)
    private LocalDateTime requestedAt;

    // 요청 활성 여부: 1=활성, 0=비활성(취소/만료). 매칭 대상 조회 시 1인 행만 사용한다.
    @Column(name = "active_request_flag", nullable = false)
    private Integer activeRequestFlag;

    @Column(name = "matched_at")
    private LocalDateTime matchedAt;

    public Long getRequestId() {
        return requestId;
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

    public Long getStartZoneId() {
        return startZoneId;
    }

    public void setStartZoneId(Long startZoneId) {
        this.startZoneId = startZoneId;
    }

    public Long getTargetZoneId() {
        return targetZoneId;
    }

    public void setTargetZoneId(Long targetZoneId) {
        this.targetZoneId = targetZoneId;
    }

    public String getDirectionType() {
        return directionType;
    }

    public void setDirectionType(String directionType) {
        this.directionType = directionType;
    }

    public String getRequestStatus() {
        return requestStatus;
    }

    public void setRequestStatus(String requestStatus) {
        this.requestStatus = requestStatus;
    }

    public LocalDateTime getDesiredDepartureAt() {
        return desiredDepartureAt;
    }

    public void setDesiredDepartureAt(LocalDateTime desiredDepartureAt) {
        this.desiredDepartureAt = desiredDepartureAt;
    }

    public Integer getMaxWaitMinutes() {
        return maxWaitMinutes;
    }

    public void setMaxWaitMinutes(Integer maxWaitMinutes) {
        this.maxWaitMinutes = maxWaitMinutes;
    }

    public LocalDateTime getRequestedAt() {
        return requestedAt;
    }

    public void setRequestedAt(LocalDateTime requestedAt) {
        this.requestedAt = requestedAt;
    }

    public Integer getActiveRequestFlag() {
        return activeRequestFlag;
    }

    public void setActiveRequestFlag(Integer activeRequestFlag) {
        this.activeRequestFlag = activeRequestFlag;
    }

    public LocalDateTime getMatchedAt() {
        return matchedAt;
    }

    public void setMatchedAt(LocalDateTime matchedAt) {
        this.matchedAt = matchedAt;
    }
}

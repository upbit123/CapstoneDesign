package com.capstone.taxiApp.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "match_groups")
public class MatchGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "match_group_id")
    private Long matchGroupId;

    @Column(name = "university_id", nullable = false)
    private Long universityId;

    @Column(name = "start_zone_id", nullable = false)
    private Long startZoneId;

    @Column(name = "target_zone_id", nullable = false)
    private Long targetZoneId;

    @Column(name = "direction_type", nullable = false)
    private String directionType;

    @Column(name = "target_user_count", nullable = false)
    private Integer targetUserCount;

    @Column(name = "matched_user_count", nullable = false)
    private Integer matchedUserCount;

    @Column(name = "match_status", nullable = false)
    private String matchStatus;

    @Column(name = "scheduled_departure_at", nullable = false)
    private LocalDateTime scheduledDepartureAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "matched_at")
    private LocalDateTime matchedAt;

    public Long getMatchGroupId() {
        return matchGroupId;
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

    public Integer getTargetUserCount() {
        return targetUserCount;
    }

    public void setTargetUserCount(Integer targetUserCount) {
        this.targetUserCount = targetUserCount;
    }

    public Integer getMatchedUserCount() {
        return matchedUserCount;
    }

    public void setMatchedUserCount(Integer matchedUserCount) {
        this.matchedUserCount = matchedUserCount;
    }

    public String getMatchStatus() {
        return matchStatus;
    }

    public void setMatchStatus(String matchStatus) {
        this.matchStatus = matchStatus;
    }

    public LocalDateTime getScheduledDepartureAt() {
        return scheduledDepartureAt;
    }

    public void setScheduledDepartureAt(LocalDateTime scheduledDepartureAt) {
        this.scheduledDepartureAt = scheduledDepartureAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getMatchedAt() {
        return matchedAt;
    }

    public void setMatchedAt(LocalDateTime matchedAt) {
        this.matchedAt = matchedAt;
    }
}

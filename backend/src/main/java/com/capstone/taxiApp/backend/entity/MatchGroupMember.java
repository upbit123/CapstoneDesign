package com.capstone.taxiApp.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "match_group_members")
public class MatchGroupMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "match_group_member_id")
    private Long matchGroupMemberId;

    @Column(name = "match_group_id", nullable = false)
    private Long matchGroupId;

    @Column(name = "university_id", nullable = false)
    private Long universityId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "request_id", nullable = false)
    private Long requestId;

    @Column(name = "member_role", nullable = false)
    private String memberRole;

    @Column(name = "join_status", nullable = false)
    private String joinStatus;

    @Column(name = "joined_at", nullable = false)
    private LocalDateTime joinedAt;

    public Long getMatchGroupId() {
        return matchGroupId;
    }

    public Long getRequestId() {
        return requestId;
    }

    public void setMatchGroupId(Long matchGroupId) {
        this.matchGroupId = matchGroupId;
    }

    public void setUniversityId(Long universityId) {
        this.universityId = universityId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public void setRequestId(Long requestId) {
        this.requestId = requestId;
    }

    public void setMemberRole(String memberRole) {
        this.memberRole = memberRole;
    }

    public void setJoinStatus(String joinStatus) {
        this.joinStatus = joinStatus;
    }

    public void setJoinedAt(LocalDateTime joinedAt) {
        this.joinedAt = joinedAt;
    }
}

package com.capstone.taxiApp.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "chat_rooms")
public class ChatRoom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "chat_room_id")
    private Long chatRoomId;

    @Column(name = "match_group_id", nullable = false)
    private Long matchGroupId;

    @Column(name = "university_id", nullable = false)
    private Long universityId;

    @Column(name = "room_status", nullable = false)
    private String roomStatus;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public Long getChatRoomId() {
        return chatRoomId;
    }

    public void setMatchGroupId(Long matchGroupId) {
        this.matchGroupId = matchGroupId;
    }

    public void setUniversityId(Long universityId) {
        this.universityId = universityId;
    }

    public void setRoomStatus(String roomStatus) {
        this.roomStatus = roomStatus;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}

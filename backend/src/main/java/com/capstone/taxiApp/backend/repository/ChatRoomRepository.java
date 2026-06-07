package com.capstone.taxiApp.backend.repository;

import com.capstone.taxiApp.backend.entity.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
    java.util.Optional<ChatRoom> findFirstByMatchGroupId(Long matchGroupId);
}

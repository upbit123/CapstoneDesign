package com.capstone.taxiApp.backend.repository;

import com.capstone.taxiApp.backend.entity.MatchGroupMember;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MatchGroupMemberRepository extends JpaRepository<MatchGroupMember, Long> {
    java.util.Optional<MatchGroupMember> findFirstByRequestId(Long requestId);
}

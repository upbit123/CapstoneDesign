package com.capstone.taxiApp.backend.repository;

import com.capstone.taxiApp.backend.entity.MatchRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MatchRequestRepository extends JpaRepository<MatchRequest, Long> {
    Optional<MatchRequest> findFirstByUserIdAndActiveRequestFlagOrderByRequestedAtDesc(Long userId, Integer activeRequestFlag);
    Optional<MatchRequest> findByRequestIdAndUserId(Long requestId, Long userId);
    List<MatchRequest> findTop4ByUniversityIdAndStartZoneIdAndTargetZoneIdAndDirectionTypeAndRequestStatusOrderByRequestedAtAsc(
            Long universityId,
            Long startZoneId,
            Long targetZoneId,
            String directionType,
            String requestStatus
    );
}

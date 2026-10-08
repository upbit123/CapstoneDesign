package com.capstone.taxiApp.backend.repository;

import com.capstone.taxiApp.backend.entity.MatchRequest;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MatchRequestRepository extends JpaRepository<MatchRequest, Long> {
}

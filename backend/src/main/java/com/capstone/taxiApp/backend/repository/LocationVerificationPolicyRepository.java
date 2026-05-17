package com.capstone.taxiApp.backend.repository;

import com.capstone.taxiApp.backend.entity.LocationVerificationPolicy;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LocationVerificationPolicyRepository extends JpaRepository<LocationVerificationPolicy, Long> {

    // 학교별로 현재 활성화된 서비스 존(원형 정책)을 조회한다.
    List<LocationVerificationPolicy> findByUniversityIdAndIsActiveOrderByZoneIdAsc(Long universityId, Integer isActive);
}

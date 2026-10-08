package com.capstone.taxiApp.backend.repository;

import com.capstone.taxiApp.backend.entity.LocationVerificationRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

// LOCATION_VERIFICATIONS 테이블에 대한 CRUD 리포지토리.
// 기본 save()/findById() 만 사용하므로 추가 쿼리 메서드는 정의하지 않는다.
public interface LocationVerificationRecordRepository extends JpaRepository<LocationVerificationRecord, Long> {
    Optional<LocationVerificationRecord> findFirstByUserIdAndUniversityIdAndApprovedAndExpiresAtAfterOrderByVerifiedAtDesc(
            Long userId,
            Long universityId,
            Integer approved,
            LocalDateTime expiresAt
    );
}

package com.capstone.taxiApp.backend.repository;

import com.capstone.taxiApp.backend.entity.SignupEmailVerification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SignupEmailVerificationRepository extends JpaRepository<SignupEmailVerification, Long> {
    Optional<SignupEmailVerification> findFirstByStudentIdAndEmailOrderByCreatedAtDesc(String studentId, String email);
    Optional<SignupEmailVerification> findFirstByStudentIdAndVerifiedOrderByVerifiedAtDescCreatedAtDesc(
            String studentId,
            Integer verified
    );
}

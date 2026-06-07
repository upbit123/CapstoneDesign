package com.capstone.taxiApp.backend.repository;

import com.capstone.taxiApp.backend.entity.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.List;

public interface AppUserRepository extends JpaRepository<AppUser, Long> {
    Optional<AppUser> findByFirebaseUid(String firebaseUid);
    Optional<AppUser> findByEmail(String email);
    Optional<AppUser> findByStudentId(String studentId);
    List<AppUser> findByUserIdIn(List<Long> userIds);
}

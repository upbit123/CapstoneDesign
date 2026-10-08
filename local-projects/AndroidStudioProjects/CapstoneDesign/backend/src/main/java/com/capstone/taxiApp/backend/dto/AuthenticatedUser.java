package com.capstone.taxiApp.backend.dto;

// Firebase 토큰 검증 후 백엔드가 신뢰하는 인증 사용자 컨텍스트.
public record AuthenticatedUser(
        Long userId,
        Long universityId,
        String firebaseUid,
        String email,
        String accountStatus
) {
}

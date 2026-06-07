package com.capstone.taxiApp.backend.dto;

// Firebase 인증 사용자와 백엔드 app_users 연동 결과를 앱에 반환하는 응답 모델.
public record FirebaseLoginResponse(
        Long userId,
        Long universityId,
        String firebaseUid,
        String email,
        String name,
        String nickname,
        String phone,
        boolean emailVerified,
        String accountStatus,
        boolean profileCompleted,
        String message
) {
}

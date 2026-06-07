package com.capstone.taxiApp.backend.dto;

// 앱에서 내 프로필 화면을 구성할 때 사용하는 응답 모델.
public record UserProfileResponse(
        Long userId,
        Long universityId,
        String email,
        String name,
        String nickname,
        String phone,
        boolean emailVerified,
        String accountStatus,
        boolean profileCompleted
) {
}

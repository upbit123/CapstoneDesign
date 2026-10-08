package com.capstone.taxiApp.backend.dto;

import jakarta.validation.constraints.NotBlank;

// 앱이 Firebase 로그인 성공 후 백엔드에 토큰 검증과 사용자 연동을 요청할 때 사용하는 모델.
public record FirebaseLoginRequest(
        @NotBlank String idToken,
        String studentId,
        String displayName,
        String nickname
) {
}

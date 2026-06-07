package com.capstone.taxiApp.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

// 앱이 프로필 작성/수정 시 보내는 요청 모델.
public record UpdateUserProfileRequest(
        @NotBlank @Size(max = 50) String name,
        @NotBlank @Size(max = 50) String nickname,
        @NotBlank
        @Pattern(regexp = "^01[0-9]-?[0-9]{3,4}-?[0-9]{4}$", message = "휴대폰 번호 형식이 올바르지 않습니다.")
        String phone
) {
}

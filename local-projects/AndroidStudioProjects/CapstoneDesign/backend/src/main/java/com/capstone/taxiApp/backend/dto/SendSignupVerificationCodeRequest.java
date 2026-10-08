package com.capstone.taxiApp.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record SendSignupVerificationCodeRequest(
        @NotBlank String studentId,
        @NotBlank @Email String email
) {
}

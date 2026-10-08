package com.capstone.taxiApp.backend.dto;

public record SignupVerificationResponse(
        boolean verified,
        String message
) {
}

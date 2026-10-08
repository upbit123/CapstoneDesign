package com.capstone.taxiApp.backend.dto;

import jakarta.validation.constraints.NotBlank;

public record PushTokenUpdateRequest(
        @NotBlank String pushToken
) {
}

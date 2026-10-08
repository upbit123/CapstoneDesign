package com.capstone.taxiApp.backend.dto;

import java.time.LocalDateTime;

// 합승 요청 생성 결과를 반환하는 응답 모델이다.
public record CreateMatchRequestResponse(
        Long requestId,
        String requestStatus,
        LocalDateTime requestedAt,
        String message
) {
}

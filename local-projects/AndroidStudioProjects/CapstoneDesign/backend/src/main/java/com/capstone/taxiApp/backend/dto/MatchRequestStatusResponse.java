package com.capstone.taxiApp.backend.dto;

public record MatchRequestStatusResponse(
        Long requestId,
        String requestStatus,
        boolean matched,
        Long matchGroupId,
        Long chatRoomId,
        String roomTitle,
        String message
) {
}

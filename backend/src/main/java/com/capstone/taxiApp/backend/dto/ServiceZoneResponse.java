package com.capstone.taxiApp.backend.dto;

// 목적지 선택 화면에서 사용하는 서비스 존 응답 모델.
public record ServiceZoneResponse(
        Long zoneId,
        Long universityId,
        String zoneName,
        String zoneType,
        Double latitude,
        Double longitude,
        Double radiusMeters
) {
}

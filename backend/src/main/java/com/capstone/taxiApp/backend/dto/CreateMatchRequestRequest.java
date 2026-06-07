package com.capstone.taxiApp.backend.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

// 인증된 위치 기반으로 합승 요청을 생성할 때 사용하는 요청 모델이다.
public record CreateMatchRequestRequest(
        @NotNull Long startZoneId,
        @NotNull Long targetZoneId,
        @NotNull String directionType,
        @NotNull LocalDateTime desiredDepartureAt,
        @NotNull @Min(1) @Max(120) Integer maxWaitMinutes
) {
}

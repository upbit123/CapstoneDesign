package com.capstone.taxiApp.backend.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

// 앱이 /verify 엔드포인트를 호출할 때 보내는 요청 본문.
// 단말 Fused GPS로 수집한 좌표와 메타데이터를 담아 서버에서 인증 구역 판정 및 DB 저장에 사용한다.
public record VerifyLocationRequest(
        @NotNull Long userId,           // 인증 요청 사용자 ID
        @NotNull Long universityId,     // 인증 대상 학교 ID
        String requestPurpose,          // 요청 목적 코드 (예: GPS_PRECISION_VERIFY)
        @NotNull Double latitude,       // 단말 GPS 위도
        @NotNull Double longitude,      // 단말 GPS 경도
        @NotNull @DecimalMin("0.0") Double accuracyMeters,  // 단말 GPS 정확도(미터). 클수록 부정확.
        Long capturedAtEpochMillis      // 단말에서 GPS를 수집한 시각 (Unix 에폭 밀리초)
) {
}

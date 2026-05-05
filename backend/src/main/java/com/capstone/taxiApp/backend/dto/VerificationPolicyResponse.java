package com.capstone.taxiApp.backend.dto;

// /policies/{universityId} 엔드포인트가 앱으로 반환하는 인증 구역 정보.
// 앱은 이 데이터로 지도에 원형 구역을 그리고 로컬 인증 폴백 판정에도 활용한다.
public record VerificationPolicyResponse(
        Long policyId,              // 구역 고유 식별자
        Long universityId,          // 학교 ID
        Long zoneId,                // 서비스 존 ID (캠퍼스 구역은 0으로 고정)
        String policyName,          // 구역 표시 이름 (예: "인천대학교 캠퍼스")
        Double maxAccuracyMeters,   // 인증 허용 최대 GPS 오차(미터). 이 값을 초과하면 LOW_ACCURACY.
        Double centerLatitude,      // 원형 구역 중심 위도
        Double centerLongitude,     // 원형 구역 중심 경도
        Double allowedRadiusMeters  // 인증 허용 반경(미터)
) {
}

package com.capstone.taxiApp.backend.dto;

// /verify-host-device 엔드포인트 요청 본문.
// 좌표 없이 호출하면 서버가 ipapi.co로 호스트(노트북) 위치를 조회해 인증한다.
// 주로 에뮬레이터 개발 테스트 시 에뮬레이터 Location 설정 없이 현재 위치를 빠르게 확인할 때 사용한다.
public record VerifyHostDeviceLocationRequest(
        String requestPurpose       // 요청 목적 코드 (null이면 LOCATION_LOAD_TEST로 기본 처리)
) {
}

package com.capstone.taxiApp.backend.controller;

import com.capstone.taxiApp.backend.dto.VerificationPolicyResponse;
import com.capstone.taxiApp.backend.dto.VerifyHostDeviceLocationRequest;
import com.capstone.taxiApp.backend.dto.VerifyLocationRequest;
import com.capstone.taxiApp.backend.dto.VerifyLocationResponse;
import com.capstone.taxiApp.backend.service.LocationVerificationService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

// 위치 인증 관련 REST 엔드포인트 컨트롤러.
// 앱 시작 시 정책 조회, 버튼 클릭 시 인증 저장, 개발 테스트용 호스트 위치 인증 세 가지 흐름을 담당한다.
@RestController
@RequestMapping("/api/v1/location-verification")
public class LocationVerificationController {

    private final LocationVerificationService locationVerificationService;

    // 생성자 주입 방식으로 서비스를 받는다.
    public LocationVerificationController(LocationVerificationService locationVerificationService) {
        this.locationVerificationService = locationVerificationService;
    }

    // GET /api/v1/location-verification/policies/{universityId}
    // 앱 시작 시 호출되어 지도에 원형 인증 구역을 그리기 위한 정책 목록을 반환한다.
    // universities(캠퍼스 경계) + service_zones(세부 구역)를 합산해 응답한다.
    @GetMapping("/policies/{universityId}")
    public List<VerificationPolicyResponse> getPolicies(@PathVariable Long universityId) {
        return locationVerificationService.getActivePolicies(universityId);
    }

    // POST /api/v1/location-verification/verify
    // 앱 단말 GPS 좌표를 받아 인증 구역 판정 후 결과를 DB에 저장하고 반환한다.
    // 단말 GPS를 직접 사용하므로 정밀도가 가장 높다.
    @PostMapping("/verify")
    public VerifyLocationResponse verify(@Valid @RequestBody VerifyLocationRequest request) {
        return locationVerificationService.verify(request);
    }

    // POST /api/v1/location-verification/verify-host-device
    // 좌표 없이 호출하면 서버가 ipapi.co로 호스트(노트북) IP 기반 위치를 조회해 인증한다.
    // 에뮬레이터 개발 테스트 전용이며, 정확도가 낮아 상용에는 적합하지 않다.
    @PostMapping("/verify-host-device")
    public VerifyLocationResponse verifyFromHostDevice(@Valid @RequestBody VerifyHostDeviceLocationRequest request) {
        return locationVerificationService.verifyFromHostDevice(request);
    }
}

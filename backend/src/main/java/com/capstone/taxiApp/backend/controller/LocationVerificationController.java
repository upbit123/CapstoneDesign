package com.capstone.taxiApp.backend.controller;

import com.capstone.taxiApp.backend.dto.AuthenticatedUser;
import com.capstone.taxiApp.backend.dto.VerificationPolicyResponse;
import com.capstone.taxiApp.backend.dto.VerifyHostDeviceLocationRequest;
import com.capstone.taxiApp.backend.dto.VerifyLocationRequest;
import com.capstone.taxiApp.backend.dto.VerifyLocationResponse;
import com.capstone.taxiApp.backend.service.LocationVerificationService;
import com.capstone.taxiApp.backend.service.RequestAuthenticationService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

// 위치 인증 관련 REST 엔드포인트 컨트롤러.
// 앱 시작 시 정책 조회, 버튼 클릭 시 인증 저장, 개발 테스트용 호스트 위치 인증 세 가지 흐름을 담당한다.
@RestController
@RequestMapping("/api/v1/location-verification")
public class LocationVerificationController {

    private final LocationVerificationService locationVerificationService;
    private final RequestAuthenticationService requestAuthenticationService;

    // 생성자 주입 방식으로 서비스를 받는다.
    public LocationVerificationController(
            LocationVerificationService locationVerificationService,
            RequestAuthenticationService requestAuthenticationService
    ) {
        this.locationVerificationService = locationVerificationService;
        this.requestAuthenticationService = requestAuthenticationService;
    }

    // GET /api/v1/location-verification/policies/me
    // 로그인된 사용자의 학교 기준으로 지도 인증 구역을 반환한다.
    @GetMapping("/policies/me")
    public List<VerificationPolicyResponse> getPolicies(
            @RequestHeader("Authorization") String authorizationHeader
    ) {
        AuthenticatedUser authenticatedUser = requestAuthenticationService.authenticate(authorizationHeader);
        return locationVerificationService.getActivePolicies(authenticatedUser.universityId());
    }

    // POST /api/v1/location-verification/verify
    // 앱 단말 GPS 좌표를 받아 인증 구역 판정 후 결과를 DB에 저장하고 반환한다.
    // 사용자 식별은 Authorization Bearer 토큰 기준으로만 처리한다.
    @PostMapping("/verify")
    public VerifyLocationResponse verify(
            @RequestHeader("Authorization") String authorizationHeader,
            @Valid @RequestBody VerifyLocationRequest request
    ) {
        AuthenticatedUser authenticatedUser = requestAuthenticationService.authenticate(authorizationHeader);
        return locationVerificationService.verify(authenticatedUser, request);
    }

    // POST /api/v1/location-verification/verify-host-device
    // 좌표 없이 호출하면 서버가 ipapi.co로 호스트(노트북) IP 기반 위치를 조회해 인증한다.
    // 에뮬레이터 개발 테스트 전용이며, 정확도가 낮아 상용에는 적합하지 않다.
    @PostMapping("/verify-host-device")
    public VerifyLocationResponse verifyFromHostDevice(
            @RequestHeader("Authorization") String authorizationHeader,
            @Valid @RequestBody VerifyHostDeviceLocationRequest request
    ) {
        AuthenticatedUser authenticatedUser = requestAuthenticationService.authenticate(authorizationHeader);
        return locationVerificationService.verifyFromHostDevice(authenticatedUser, request);
    }
}

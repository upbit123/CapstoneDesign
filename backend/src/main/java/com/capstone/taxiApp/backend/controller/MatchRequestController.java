package com.capstone.taxiApp.backend.controller;

import com.capstone.taxiApp.backend.dto.AuthenticatedUser;
import com.capstone.taxiApp.backend.dto.CreateMatchRequestRequest;
import com.capstone.taxiApp.backend.dto.CreateMatchRequestResponse;
import com.capstone.taxiApp.backend.dto.MatchRequestStatusResponse;
import com.capstone.taxiApp.backend.service.MatchRequestService;
import com.capstone.taxiApp.backend.service.RequestAuthenticationService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// 합승 요청 관련 REST 엔드포인트 컨트롤러.
// 위치 인증이 성공한 사용자만 앱에서 이 엔드포인트를 호출해 합승 요청을 생성한다.
@RestController
@RequestMapping("/api/v1/match-requests")
public class MatchRequestController {

    private final MatchRequestService matchRequestService;
    private final RequestAuthenticationService requestAuthenticationService;

    // 생성자 주입 방식으로 서비스를 받아 테스트 시 Mock으로 교체 가능하게 한다.
    public MatchRequestController(
            MatchRequestService matchRequestService,
            RequestAuthenticationService requestAuthenticationService
    ) {
        this.matchRequestService = matchRequestService;
        this.requestAuthenticationService = requestAuthenticationService;
    }

    // POST /api/v1/match-requests
    // 합승 요청을 생성하고 요청 ID와 상태를 반환한다.
    // 사용자 식별은 Authorization Bearer 토큰 기준으로만 처리한다.
    @PostMapping
    public CreateMatchRequestResponse create(
            @RequestHeader("Authorization") String authorizationHeader,
            @Valid @RequestBody CreateMatchRequestRequest request
    ) {
        AuthenticatedUser authenticatedUser = requestAuthenticationService.authenticate(authorizationHeader);
        return matchRequestService.create(authenticatedUser, request);
    }

    @GetMapping("/{requestId}")
    public MatchRequestStatusResponse getStatus(
            @RequestHeader("Authorization") String authorizationHeader,
            @PathVariable long requestId
    ) {
        AuthenticatedUser authenticatedUser = requestAuthenticationService.authenticate(authorizationHeader);
        return matchRequestService.getMyRequestStatus(authenticatedUser, requestId);
    }
}

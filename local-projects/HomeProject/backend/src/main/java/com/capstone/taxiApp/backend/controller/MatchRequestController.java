package com.capstone.taxiApp.backend.controller;

import com.capstone.taxiApp.backend.dto.CreateMatchRequestRequest;
import com.capstone.taxiApp.backend.dto.CreateMatchRequestResponse;
import com.capstone.taxiApp.backend.service.MatchRequestService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// 합승 요청 관련 REST 엔드포인트 컨트롤러.
// 위치 인증이 성공한 사용자만 앱에서 이 엔드포인트를 호출해 합승 요청을 생성한다.
@RestController
@RequestMapping("/api/v1/match-requests")
public class MatchRequestController {

    private final MatchRequestService matchRequestService;

    // 생성자 주입 방식으로 서비스를 받아 테스트 시 Mock으로 교체 가능하게 한다.
    public MatchRequestController(MatchRequestService matchRequestService) {
        this.matchRequestService = matchRequestService;
    }

    // POST /api/v1/match-requests
    // 합승 요청을 생성하고 요청 ID와 상태를 반환한다.
    // 위치 인증이 유효한 사용자만 앱에서 이 엔드포인트를 호출하도록 앱 레벨에서 제어한다.
    @PostMapping
    public CreateMatchRequestResponse create(@Valid @RequestBody CreateMatchRequestRequest request) {
        return matchRequestService.create(request);
    }
}

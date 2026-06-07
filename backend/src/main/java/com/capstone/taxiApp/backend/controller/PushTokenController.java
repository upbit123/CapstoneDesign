package com.capstone.taxiApp.backend.controller;

import com.capstone.taxiApp.backend.dto.AuthenticatedUser;
import com.capstone.taxiApp.backend.dto.PushTokenUpdateRequest;
import com.capstone.taxiApp.backend.service.PushTokenService;
import com.capstone.taxiApp.backend.service.RequestAuthenticationService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/me")
public class PushTokenController {

    private final RequestAuthenticationService requestAuthenticationService;
    private final PushTokenService pushTokenService;

    public PushTokenController(
            RequestAuthenticationService requestAuthenticationService,
            PushTokenService pushTokenService
    ) {
        this.requestAuthenticationService = requestAuthenticationService;
        this.pushTokenService = pushTokenService;
    }

    @PostMapping("/push-token")
    public Map<String, String> updatePushToken(
            @RequestHeader("Authorization") String authorizationHeader,
            @Valid @RequestBody PushTokenUpdateRequest request
    ) {
        AuthenticatedUser authenticatedUser = requestAuthenticationService.authenticate(authorizationHeader);
        pushTokenService.updateMyPushToken(authenticatedUser, request.pushToken());
        return Map.of("message", "푸시 토큰이 저장되었습니다.");
    }
}

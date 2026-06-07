package com.capstone.taxiApp.backend.controller;

import com.capstone.taxiApp.backend.dto.FirebaseLoginRequest;
import com.capstone.taxiApp.backend.dto.FirebaseLoginResponse;
import com.capstone.taxiApp.backend.service.FirebaseAuthenticationService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// Firebase 로그인 성공 후 앱 사용자를 백엔드 app_users와 연결하는 인증 컨트롤러.
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final FirebaseAuthenticationService firebaseAuthenticationService;

    public AuthController(FirebaseAuthenticationService firebaseAuthenticationService) {
        this.firebaseAuthenticationService = firebaseAuthenticationService;
    }

    @PostMapping("/firebase-login")
    public FirebaseLoginResponse firebaseLogin(@Valid @RequestBody FirebaseLoginRequest request) {
        return firebaseAuthenticationService.loginOrRegister(request);
    }
}

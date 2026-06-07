package com.capstone.taxiApp.backend.controller;

import com.capstone.taxiApp.backend.dto.SendSignupVerificationCodeRequest;
import com.capstone.taxiApp.backend.dto.SignupVerificationResponse;
import com.capstone.taxiApp.backend.dto.VerifySignupCodeRequest;
import com.capstone.taxiApp.backend.service.SignupVerificationService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/public/signup-verifications")
public class SignupVerificationController {

    private final SignupVerificationService signupVerificationService;

    public SignupVerificationController(SignupVerificationService signupVerificationService) {
        this.signupVerificationService = signupVerificationService;
    }

    @PostMapping("/send-code")
    public SignupVerificationResponse sendCode(@Valid @RequestBody SendSignupVerificationCodeRequest request) {
        return signupVerificationService.sendSignupVerificationCode(request);
    }

    @PostMapping("/verify-code")
    public SignupVerificationResponse verifyCode(@Valid @RequestBody VerifySignupCodeRequest request) {
        return signupVerificationService.verifySignupCode(request);
    }
}

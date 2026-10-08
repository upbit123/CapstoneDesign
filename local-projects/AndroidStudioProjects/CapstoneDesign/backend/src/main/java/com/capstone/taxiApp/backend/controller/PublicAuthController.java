package com.capstone.taxiApp.backend.controller;

import com.capstone.taxiApp.backend.dto.StudentLoginLookupRequest;
import com.capstone.taxiApp.backend.dto.StudentLoginLookupResponse;
import com.capstone.taxiApp.backend.entity.AppUser;
import com.capstone.taxiApp.backend.entity.SignupEmailVerification;
import com.capstone.taxiApp.backend.repository.AppUserRepository;
import com.capstone.taxiApp.backend.repository.SignupEmailVerificationRepository;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/public/auth")
public class PublicAuthController {

    private final AppUserRepository appUserRepository;
    private final SignupEmailVerificationRepository signupEmailVerificationRepository;

    public PublicAuthController(
            AppUserRepository appUserRepository,
            SignupEmailVerificationRepository signupEmailVerificationRepository
    ) {
        this.appUserRepository = appUserRepository;
        this.signupEmailVerificationRepository = signupEmailVerificationRepository;
    }

    @PostMapping("/student-login-email")
    public StudentLoginLookupResponse lookupEmail(@Valid @RequestBody StudentLoginLookupRequest request) {
        String studentId = request.studentId().trim();

        AppUser appUser = appUserRepository.findByStudentId(studentId).orElse(null);
        if (appUser != null) {
            return new StudentLoginLookupResponse(appUser.getStudentId(), appUser.getEmail());
        }

        SignupEmailVerification verification = signupEmailVerificationRepository
                .findFirstByStudentIdAndVerifiedOrderByVerifiedAtDescCreatedAtDesc(studentId, 1)
                .orElseThrow(() -> new IllegalArgumentException("등록된 학번을 찾을 수 없습니다."));

        return new StudentLoginLookupResponse(studentId, verification.getEmail());
    }
}

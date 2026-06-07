package com.capstone.taxiApp.backend.service;

import com.capstone.taxiApp.backend.dto.AuthenticatedUser;
import com.capstone.taxiApp.backend.entity.AppUser;
import com.capstone.taxiApp.backend.repository.AppUserRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
public class RequestAuthenticationService {

    private static final String ACCOUNT_STATUS_ACTIVE = "ACTIVE";
    private static final String BEARER_PREFIX = "Bearer ";

    private final AppUserRepository appUserRepository;

    public RequestAuthenticationService(AppUserRepository appUserRepository) {
        this.appUserRepository = appUserRepository;
    }

    public AuthenticatedUser authenticate(String authorizationHeader) {
        String idToken = extractBearerToken(authorizationHeader);
        FirebaseToken firebaseToken = verifyIdToken(idToken);

        String email = normalizeEmail(firebaseToken.getEmail());
        AppUser appUser = appUserRepository.findByFirebaseUid(firebaseToken.getUid())
                .or(() -> appUserRepository.findByEmail(email))
                .orElseThrow(() -> new IllegalArgumentException("백엔드 사용자 계정을 먼저 연동해 주세요."));

        if (!ACCOUNT_STATUS_ACTIVE.equals(appUser.getAccountStatus())) {
            throw new IllegalArgumentException("사용할 수 없는 계정 상태입니다: " + appUser.getAccountStatus());
        }

        return new AuthenticatedUser(
                appUser.getUserId(),
                appUser.getUniversityId(),
                appUser.getFirebaseUid(),
                appUser.getEmail(),
                appUser.getAccountStatus()
        );
    }

    private String extractBearerToken(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith(BEARER_PREFIX)) {
            throw new IllegalArgumentException("Authorization Bearer 토큰이 필요합니다.");
        }

        String token = authorizationHeader.substring(BEARER_PREFIX.length()).trim();
        if (token.isBlank()) {
            throw new IllegalArgumentException("Authorization Bearer 토큰이 비어 있습니다.");
        }
        return token;
    }

    private FirebaseToken verifyIdToken(String idToken) {
        try {
            return FirebaseAuth.getInstance().verifyIdToken(idToken);
        } catch (FirebaseAuthException exception) {
            throw new IllegalArgumentException("Firebase 토큰 검증에 실패했습니다.");
        }
    }

    private String normalizeEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
    }
}

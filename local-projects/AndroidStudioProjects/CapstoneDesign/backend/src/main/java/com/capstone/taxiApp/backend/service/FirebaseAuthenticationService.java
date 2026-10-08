package com.capstone.taxiApp.backend.service;

import com.capstone.taxiApp.backend.dto.FirebaseLoginRequest;
import com.capstone.taxiApp.backend.dto.FirebaseLoginResponse;
import com.capstone.taxiApp.backend.entity.AppUser;
import com.capstone.taxiApp.backend.entity.University;
import com.capstone.taxiApp.backend.repository.AppUserRepository;
import com.capstone.taxiApp.backend.repository.UniversityRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
public class FirebaseAuthenticationService {

    private static final String ACCOUNT_STATUS_ACTIVE = "ACTIVE";

    private final AppUserRepository appUserRepository;
    private final UniversityRepository universityRepository;

    public FirebaseAuthenticationService(
            AppUserRepository appUserRepository,
            UniversityRepository universityRepository
    ) {
        this.appUserRepository = appUserRepository;
        this.universityRepository = universityRepository;
    }

    @Transactional
    public FirebaseLoginResponse loginOrRegister(FirebaseLoginRequest request) {
        FirebaseToken firebaseToken = verifyIdToken(request.idToken());

        String email = normalizeEmail(firebaseToken.getEmail());
        if (email.isBlank()) {
            throw new IllegalArgumentException("Firebase 계정에 이메일 정보가 없습니다.");
        }

        String domain = extractEmailDomain(email);
        University university = universityRepository.findBySchoolEmailDomainIgnoreCase(domain)
                .orElseThrow(() -> new IllegalArgumentException("등록되지 않은 학교 이메일 도메인입니다."));

        AppUser appUser = appUserRepository.findByFirebaseUid(firebaseToken.getUid())
                .or(() -> appUserRepository.findByEmail(email))
                .or(() -> findByStudentId(request.studentId()))
                .orElseGet(AppUser::new);

        boolean isNewUser = appUser.getUserId() == null;
        String resolvedName = resolveName(firebaseToken, request, email);
        String resolvedNickname = resolveNickname(request, resolvedName);

        appUser.setUniversityId(university.getUniversityId());
        if (hasText(request.studentId())) {
            appUser.setStudentId(request.studentId().trim());
        }
        appUser.setFirebaseUid(firebaseToken.getUid());
        appUser.setEmail(email);
        appUser.setName(resolvedName);
        appUser.setNickname(resolvedNickname);
        appUser.setIsEmailVerified(Boolean.TRUE.equals(firebaseToken.isEmailVerified()) ? 1 : 0);
        if (appUser.getAccountStatus() == null || appUser.getAccountStatus().isBlank()) {
            appUser.setAccountStatus(ACCOUNT_STATUS_ACTIVE);
        }

        if (!ACCOUNT_STATUS_ACTIVE.equals(appUser.getAccountStatus())) {
            throw new IllegalArgumentException("사용할 수 없는 계정 상태입니다: " + appUser.getAccountStatus());
        }

        AppUser savedUser = appUserRepository.save(appUser);

        return new FirebaseLoginResponse(
                savedUser.getUserId(),
                savedUser.getUniversityId(),
                savedUser.getFirebaseUid(),
                savedUser.getEmail(),
                savedUser.getName(),
                savedUser.getNickname(),
                savedUser.getPhone(),
                savedUser.getIsEmailVerified() == 1,
                savedUser.getAccountStatus(),
                isProfileCompleted(savedUser),
                isNewUser ? "백엔드 사용자 계정을 생성했습니다." : "백엔드 사용자 계정을 확인했습니다."
        );
    }

    private FirebaseToken verifyIdToken(String idToken) {
        try {
            return FirebaseAuth.getInstance().verifyIdToken(idToken);
        } catch (FirebaseAuthException exception) {
            throw new IllegalArgumentException("Firebase 토큰 검증에 실패했습니다.");
        }
    }

    private String resolveName(FirebaseToken firebaseToken, FirebaseLoginRequest request, String email) {
        String claimedName = normalizeText(firebaseToken.getName());
        if (!claimedName.isBlank()) {
            return claimedName;
        }

        String requestDisplayName = normalizeText(request.displayName());
        if (!requestDisplayName.isBlank()) {
            return requestDisplayName;
        }

        return email.substring(0, email.indexOf('@'));
    }

    private String resolveNickname(FirebaseLoginRequest request, String fallbackName) {
        String nickname = normalizeText(request.nickname());
        return nickname.isBlank() ? fallbackName : nickname;
    }

    private String normalizeEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
    }

    private String extractEmailDomain(String email) {
        int separatorIndex = email.indexOf('@');
        if (separatorIndex < 0 || separatorIndex == email.length() - 1) {
            throw new IllegalArgumentException("이메일 형식이 올바르지 않습니다.");
        }
        return email.substring(separatorIndex + 1);
    }

    private String normalizeText(String value) {
        return value == null ? "" : value.trim();
    }

    private boolean isProfileCompleted(AppUser appUser) {
        return hasText(appUser.getName())
                && hasText(appUser.getNickname())
                && hasText(appUser.getPhone());
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private java.util.Optional<AppUser> findByStudentId(String studentId) {
        if (!hasText(studentId)) {
            return java.util.Optional.empty();
        }
        return appUserRepository.findByStudentId(studentId.trim());
    }
}

package com.capstone.taxiApp.backend.service;

import com.capstone.taxiApp.backend.dto.AuthenticatedUser;
import com.capstone.taxiApp.backend.dto.UpdateUserProfileRequest;
import com.capstone.taxiApp.backend.dto.UserProfileResponse;
import com.capstone.taxiApp.backend.entity.AppUser;
import com.capstone.taxiApp.backend.repository.AppUserRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
public class UserProfileService {

    private final AppUserRepository appUserRepository;

    public UserProfileService(AppUserRepository appUserRepository) {
        this.appUserRepository = appUserRepository;
    }

    @Transactional
    public UserProfileResponse getMyProfile(AuthenticatedUser authenticatedUser) {
        AppUser appUser = findUser(authenticatedUser.userId());
        return toResponse(appUser);
    }

    @Transactional
    public UserProfileResponse updateMyProfile(
            AuthenticatedUser authenticatedUser,
            UpdateUserProfileRequest request
    ) {
        AppUser appUser = findUser(authenticatedUser.userId());
        appUser.setName(request.name().trim());
        appUser.setNickname(request.nickname().trim());
        appUser.setPhone(normalizePhone(request.phone()));
        return toResponse(appUserRepository.save(appUser));
    }

    private AppUser findUser(Long userId) {
        return appUserRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자 정보를 찾을 수 없습니다."));
    }

    private UserProfileResponse toResponse(AppUser appUser) {
        return new UserProfileResponse(
                appUser.getUserId(),
                appUser.getUniversityId(),
                appUser.getEmail(),
                appUser.getName(),
                appUser.getNickname(),
                appUser.getPhone(),
                appUser.getIsEmailVerified() == 1,
                appUser.getAccountStatus(),
                isProfileCompleted(appUser)
        );
    }

    private boolean isProfileCompleted(AppUser appUser) {
        return hasText(appUser.getName())
                && hasText(appUser.getNickname())
                && hasText(appUser.getPhone());
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private String normalizePhone(String phone) {
        return phone.trim().replace(" ", "");
    }
}

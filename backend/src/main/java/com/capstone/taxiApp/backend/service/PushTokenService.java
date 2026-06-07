package com.capstone.taxiApp.backend.service;

import com.capstone.taxiApp.backend.dto.AuthenticatedUser;
import com.capstone.taxiApp.backend.repository.AppUserRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
public class PushTokenService {

    private final AppUserRepository appUserRepository;

    public PushTokenService(AppUserRepository appUserRepository) {
        this.appUserRepository = appUserRepository;
    }

    @Transactional
    public void updateMyPushToken(AuthenticatedUser authenticatedUser, String pushToken) {
        appUserRepository.findById(authenticatedUser.userId())
                .ifPresent(appUser -> {
                    appUser.setPushToken(pushToken.trim());
                    appUserRepository.save(appUser);
                });
    }
}

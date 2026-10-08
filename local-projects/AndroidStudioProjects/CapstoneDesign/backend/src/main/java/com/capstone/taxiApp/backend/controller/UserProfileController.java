package com.capstone.taxiApp.backend.controller;

import com.capstone.taxiApp.backend.dto.AuthenticatedUser;
import com.capstone.taxiApp.backend.dto.UpdateUserProfileRequest;
import com.capstone.taxiApp.backend.dto.UserProfileResponse;
import com.capstone.taxiApp.backend.service.RequestAuthenticationService;
import com.capstone.taxiApp.backend.service.UserProfileService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users/me")
public class UserProfileController {

    private final RequestAuthenticationService requestAuthenticationService;
    private final UserProfileService userProfileService;

    public UserProfileController(
            RequestAuthenticationService requestAuthenticationService,
            UserProfileService userProfileService
    ) {
        this.requestAuthenticationService = requestAuthenticationService;
        this.userProfileService = userProfileService;
    }

    @GetMapping
    public UserProfileResponse getMyProfile(
            @RequestHeader("Authorization") String authorizationHeader
    ) {
        AuthenticatedUser authenticatedUser = requestAuthenticationService.authenticate(authorizationHeader);
        return userProfileService.getMyProfile(authenticatedUser);
    }

    @PutMapping("/profile")
    public UserProfileResponse updateMyProfile(
            @RequestHeader("Authorization") String authorizationHeader,
            @Valid @RequestBody UpdateUserProfileRequest request
    ) {
        AuthenticatedUser authenticatedUser = requestAuthenticationService.authenticate(authorizationHeader);
        return userProfileService.updateMyProfile(authenticatedUser, request);
    }
}

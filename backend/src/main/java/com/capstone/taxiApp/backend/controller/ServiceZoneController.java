package com.capstone.taxiApp.backend.controller;

import com.capstone.taxiApp.backend.dto.AuthenticatedUser;
import com.capstone.taxiApp.backend.dto.ServiceZoneResponse;
import com.capstone.taxiApp.backend.service.RequestAuthenticationService;
import com.capstone.taxiApp.backend.service.ServiceZoneService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/service-zones")
public class ServiceZoneController {

    private final RequestAuthenticationService requestAuthenticationService;
    private final ServiceZoneService serviceZoneService;

    public ServiceZoneController(
            RequestAuthenticationService requestAuthenticationService,
            ServiceZoneService serviceZoneService
    ) {
        this.requestAuthenticationService = requestAuthenticationService;
        this.serviceZoneService = serviceZoneService;
    }

    @GetMapping("/me")
    public List<ServiceZoneResponse> getMyServiceZones(
            @RequestHeader("Authorization") String authorizationHeader
    ) {
        AuthenticatedUser authenticatedUser = requestAuthenticationService.authenticate(authorizationHeader);
        return serviceZoneService.getActiveServiceZones(authenticatedUser.universityId());
    }
}

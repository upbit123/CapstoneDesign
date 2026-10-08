package com.capstone.taxiApp.backend.service;

import com.capstone.taxiApp.backend.dto.ServiceZoneResponse;
import com.capstone.taxiApp.backend.entity.LocationVerificationPolicy;
import com.capstone.taxiApp.backend.repository.LocationVerificationPolicyRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ServiceZoneService {

    private final LocationVerificationPolicyRepository locationVerificationPolicyRepository;

    public ServiceZoneService(LocationVerificationPolicyRepository locationVerificationPolicyRepository) {
        this.locationVerificationPolicyRepository = locationVerificationPolicyRepository;
    }

    public List<ServiceZoneResponse> getActiveServiceZones(Long universityId) {
        return locationVerificationPolicyRepository.findByUniversityIdAndIsActiveOrderByZoneIdAsc(universityId, 1)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private ServiceZoneResponse toResponse(LocationVerificationPolicy policy) {
        return new ServiceZoneResponse(
                policy.getZoneId(),
                policy.getUniversityId(),
                policy.getPolicyName(),
                policy.getVerificationType(),
                policy.getCenterLatitude().doubleValue(),
                policy.getCenterLongitude().doubleValue(),
                policy.getAllowedRadiusM().doubleValue()
        );
    }
}

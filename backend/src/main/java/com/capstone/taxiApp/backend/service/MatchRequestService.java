package com.capstone.taxiApp.backend.service;

import com.capstone.taxiApp.backend.dto.CreateMatchRequestRequest;
import com.capstone.taxiApp.backend.dto.CreateMatchRequestResponse;
import com.capstone.taxiApp.backend.entity.MatchRequest;
import com.capstone.taxiApp.backend.repository.MatchRequestRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

// 합승 요청 생성 비즈니스 로직 서비스.
// 위치 인증이 완료된 사용자가 택시 합승을 요청할 때 호출된다.
@Service
public class MatchRequestService {

    private final MatchRequestRepository matchRequestRepository;

    // 생성자 주입 방식으로 리포지토리를 받는다.
    public MatchRequestService(MatchRequestRepository matchRequestRepository) {
        this.matchRequestRepository = matchRequestRepository;
    }

    // 합승 요청 엔티티를 생성해 DB에 저장하고 요청 ID·상태·시각을 반환한다.
    // 초기 상태는 항상 WAITING, 활성 플래그는 1로 설정된다.
    @Transactional
    public CreateMatchRequestResponse create(CreateMatchRequestRequest request) {
        MatchRequest entity = new MatchRequest();
        entity.setUserId(request.userId());
        entity.setUniversityId(request.universityId());
        entity.setStartZoneId(request.startZoneId());
        entity.setTargetZoneId(request.targetZoneId());
        entity.setDirectionType(request.directionType());
        // 생성 직후 상태는 WAITING(매칭 대기 중)으로 고정한다.
        entity.setRequestStatus("WAITING");
        entity.setDesiredDepartureAt(request.desiredDepartureAt());
        entity.setMaxWaitMinutes(request.maxWaitMinutes());
        // 서버 시각 기준으로 요청 접수 시각을 기록한다.
        entity.setRequestedAt(LocalDateTime.now());
        // 1=활성. 취소/만료 시 0으로 변경해 매칭 대상에서 제외한다.
        entity.setActiveRequestFlag(1);

        MatchRequest saved = matchRequestRepository.save(entity);

        return new CreateMatchRequestResponse(
                saved.getRequestId(),
                saved.getRequestStatus(),
                saved.getRequestedAt(),
                "매칭 요청이 생성되었습니다."
        );
    }
}

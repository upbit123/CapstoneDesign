package com.capstone.taxiApp.backend.service;

import com.capstone.taxiApp.backend.dto.AuthenticatedUser;
import com.capstone.taxiApp.backend.dto.CreateMatchRequestRequest;
import com.capstone.taxiApp.backend.dto.CreateMatchRequestResponse;
import com.capstone.taxiApp.backend.dto.MatchRequestStatusResponse;
import com.capstone.taxiApp.backend.entity.ChatRoom;
import com.capstone.taxiApp.backend.entity.LocationVerificationPolicy;
import com.capstone.taxiApp.backend.entity.LocationVerificationRecord;
import com.capstone.taxiApp.backend.entity.MatchGroup;
import com.capstone.taxiApp.backend.entity.MatchGroupMember;
import com.capstone.taxiApp.backend.entity.MatchRequest;
import com.capstone.taxiApp.backend.repository.ChatRoomRepository;
import com.capstone.taxiApp.backend.repository.LocationVerificationPolicyRepository;
import com.capstone.taxiApp.backend.repository.LocationVerificationRecordRepository;
import com.capstone.taxiApp.backend.repository.MatchGroupMemberRepository;
import com.capstone.taxiApp.backend.repository.MatchGroupRepository;
import com.capstone.taxiApp.backend.repository.MatchRequestRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

// 합승 요청 생성 비즈니스 로직 서비스.
// 위치 인증이 완료된 사용자가 택시 합승을 요청할 때 호출된다.
@Service
public class MatchRequestService {

    private static final int ACTIVE_REQUEST_FLAG_ON = 1;
    private static final int ACTIVE_REQUEST_FLAG_OFF = 0;
    private static final int APPROVED_VALUE = 1;
    private static final int TARGET_USER_COUNT = 4;
    private static final String STATUS_WAITING = "WAITING";
    private static final String STATUS_MATCHED = "MATCHED";
    private static final String GROUP_STATUS_COMPLETED = "COMPLETED";
    private static final String CHAT_ROOM_STATUS_OPEN = "OPEN";
    private static final String MEMBER_ROLE_LEADER = "LEADER";
    private static final String MEMBER_ROLE_MEMBER = "MEMBER";
    private static final String JOIN_STATUS_JOINED = "JOINED";

    private final MatchRequestRepository matchRequestRepository;
    private final LocationVerificationRecordRepository locationVerificationRecordRepository;
    private final MatchGroupRepository matchGroupRepository;
    private final MatchGroupMemberRepository matchGroupMemberRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final LocationVerificationPolicyRepository locationVerificationPolicyRepository;
    private final MatchNotificationService matchNotificationService;

    // 생성자 주입 방식으로 리포지토리를 받는다.
    public MatchRequestService(
            MatchRequestRepository matchRequestRepository,
            LocationVerificationRecordRepository locationVerificationRecordRepository,
            MatchGroupRepository matchGroupRepository,
            MatchGroupMemberRepository matchGroupMemberRepository,
            ChatRoomRepository chatRoomRepository,
            LocationVerificationPolicyRepository locationVerificationPolicyRepository,
            MatchNotificationService matchNotificationService
    ) {
        this.matchRequestRepository = matchRequestRepository;
        this.locationVerificationRecordRepository = locationVerificationRecordRepository;
        this.matchGroupRepository = matchGroupRepository;
        this.matchGroupMemberRepository = matchGroupMemberRepository;
        this.chatRoomRepository = chatRoomRepository;
        this.locationVerificationPolicyRepository = locationVerificationPolicyRepository;
        this.matchNotificationService = matchNotificationService;
    }

    // 합승 요청 엔티티를 생성해 DB에 저장하고 요청 ID·상태·시각을 반환한다.
    // 초기 상태는 항상 WAITING, 활성 플래그는 1로 설정된다.
    @Transactional
    public CreateMatchRequestResponse create(AuthenticatedUser authenticatedUser, CreateMatchRequestRequest request) {
        validateRequest(authenticatedUser, request);

        MatchRequest entity = new MatchRequest();
        entity.setUserId(authenticatedUser.userId());
        entity.setUniversityId(authenticatedUser.universityId());
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
        entity.setActiveRequestFlag(ACTIVE_REQUEST_FLAG_ON);

        MatchRequest saved = matchRequestRepository.save(entity);
        MatchCompletionResult completionResult = tryCompleteMatch(saved);

        return new CreateMatchRequestResponse(
                saved.getRequestId(),
                saved.getRequestStatus(),
                completionResult.matched(),
                completionResult.matchGroupId(),
                completionResult.chatRoomId(),
                completionResult.roomTitle(),
                saved.getRequestedAt(),
                completionResult.matched()
                        ? "4인 매칭이 완료되었습니다."
                        : "매칭 요청이 생성되었습니다."
        );
    }

    @Transactional
    public MatchRequestStatusResponse getMyRequestStatus(AuthenticatedUser authenticatedUser, long requestId) {
        MatchRequest request = matchRequestRepository.findByRequestIdAndUserId(requestId, authenticatedUser.userId())
                .orElseThrow(() -> new IllegalArgumentException("해당 매칭 요청을 찾을 수 없습니다."));

        if (!STATUS_MATCHED.equals(request.getRequestStatus())) {
            return new MatchRequestStatusResponse(
                    request.getRequestId(),
                    request.getRequestStatus(),
                    false,
                    null,
                    null,
                    null,
                    "아직 매칭 대기 중입니다."
            );
        }

        MatchGroupMember member = matchGroupMemberRepository.findFirstByRequestId(request.getRequestId())
                .orElseThrow(() -> new IllegalArgumentException("매칭 그룹 정보를 찾을 수 없습니다."));
        ChatRoom chatRoom = chatRoomRepository.findFirstByMatchGroupId(member.getMatchGroupId())
                .orElseThrow(() -> new IllegalArgumentException("채팅방 정보를 찾을 수 없습니다."));
        String roomTitle = buildRoomTitle(request.getStartZoneId(), request.getTargetZoneId());

        return new MatchRequestStatusResponse(
                request.getRequestId(),
                request.getRequestStatus(),
                true,
                member.getMatchGroupId(),
                chatRoom.getChatRoomId(),
                roomTitle,
                "매칭이 완료되었습니다."
        );
    }

    private void validateRequest(AuthenticatedUser authenticatedUser, CreateMatchRequestRequest request) {
        LocalDateTime now = LocalDateTime.now();

        matchRequestRepository.findFirstByUserIdAndActiveRequestFlagOrderByRequestedAtDesc(
                authenticatedUser.userId(),
                ACTIVE_REQUEST_FLAG_ON
        ).ifPresent(existingRequest -> {
            throw new IllegalArgumentException("이미 대기 중인 매칭 요청이 있습니다.");
        });

        LocationVerificationRecord verificationRecord =
                locationVerificationRecordRepository
                        .findFirstByUserIdAndUniversityIdAndApprovedAndExpiresAtAfterOrderByVerifiedAtDesc(
                                authenticatedUser.userId(),
                                authenticatedUser.universityId(),
                                APPROVED_VALUE,
                                now
                        )
                        .orElseThrow(() -> new IllegalArgumentException("유효한 위치 인증이 필요합니다."));

        if (verificationRecord.getMatchedZoneId() == null) {
            throw new IllegalArgumentException("인증된 출발 구역 정보가 없습니다.");
        }

        if (!verificationRecord.getMatchedZoneId().equals(request.startZoneId())) {
            throw new IllegalArgumentException("위치 인증된 구역과 출발 구역이 일치하지 않습니다.");
        }
    }

    private MatchCompletionResult tryCompleteMatch(MatchRequest savedRequest) {
        List<MatchRequest> waitingRequests =
                matchRequestRepository.findTop4ByUniversityIdAndStartZoneIdAndTargetZoneIdAndDirectionTypeAndRequestStatusOrderByRequestedAtAsc(
                        savedRequest.getUniversityId(),
                        savedRequest.getStartZoneId(),
                        savedRequest.getTargetZoneId(),
                        savedRequest.getDirectionType(),
                        STATUS_WAITING
                );

        if (waitingRequests.size() < TARGET_USER_COUNT) {
            return MatchCompletionResult.notMatched();
        }

        LocalDateTime now = LocalDateTime.now();

        MatchGroup matchGroup = new MatchGroup();
        matchGroup.setUniversityId(savedRequest.getUniversityId());
        matchGroup.setStartZoneId(savedRequest.getStartZoneId());
        matchGroup.setTargetZoneId(savedRequest.getTargetZoneId());
        matchGroup.setDirectionType(savedRequest.getDirectionType());
        matchGroup.setTargetUserCount(TARGET_USER_COUNT);
        matchGroup.setMatchedUserCount(waitingRequests.size());
        matchGroup.setMatchStatus(GROUP_STATUS_COMPLETED);
        matchGroup.setScheduledDepartureAt(savedRequest.getDesiredDepartureAt());
        matchGroup.setCreatedAt(now);
        matchGroup.setMatchedAt(now);
        MatchGroup savedGroup = matchGroupRepository.save(matchGroup);

        List<MatchGroupMember> members = new ArrayList<>();
        for (int index = 0; index < waitingRequests.size(); index++) {
            MatchRequest request = waitingRequests.get(index);
            request.setRequestStatus(STATUS_MATCHED);
            request.setActiveRequestFlag(ACTIVE_REQUEST_FLAG_OFF);
            request.setMatchedAt(now);

            MatchGroupMember member = new MatchGroupMember();
            member.setMatchGroupId(savedGroup.getMatchGroupId());
            member.setUniversityId(request.getUniversityId());
            member.setUserId(request.getUserId());
            member.setRequestId(request.getRequestId());
            member.setMemberRole(index == 0 ? MEMBER_ROLE_LEADER : MEMBER_ROLE_MEMBER);
            member.setJoinStatus(JOIN_STATUS_JOINED);
            member.setJoinedAt(now);
            members.add(member);
        }

        matchRequestRepository.saveAll(waitingRequests);
        matchGroupMemberRepository.saveAll(members);

        ChatRoom chatRoom = new ChatRoom();
        chatRoom.setMatchGroupId(savedGroup.getMatchGroupId());
        chatRoom.setUniversityId(savedGroup.getUniversityId());
        chatRoom.setRoomStatus(CHAT_ROOM_STATUS_OPEN);
        chatRoom.setCreatedAt(now);
        ChatRoom savedChatRoom = chatRoomRepository.save(chatRoom);

        String roomTitle = buildRoomTitle(savedRequest.getStartZoneId(), savedRequest.getTargetZoneId());
        List<Long> matchedUserIds = waitingRequests.stream()
                .map(MatchRequest::getUserId)
                .collect(java.util.stream.Collectors.toList());
        matchNotificationService.notifyMatchCompleted(
                matchedUserIds,
                savedChatRoom.getChatRoomId(),
                savedGroup.getMatchGroupId(),
                roomTitle
        );

        return new MatchCompletionResult(true, savedGroup.getMatchGroupId(), savedChatRoom.getChatRoomId(), roomTitle);
    }

    private String buildRoomTitle(Long startZoneId, Long targetZoneId) {
        String startZoneName = locationVerificationPolicyRepository.findById(startZoneId)
                .map(LocationVerificationPolicy::getPolicyName)
                .orElse("출발지");
        String targetZoneName = locationVerificationPolicyRepository.findById(targetZoneId)
                .map(LocationVerificationPolicy::getPolicyName)
                .orElse("목적지");
        return startZoneName + " -> " + targetZoneName;
    }

    private record MatchCompletionResult(
            boolean matched,
            Long matchGroupId,
            Long chatRoomId,
            String roomTitle
    ) {
        private static MatchCompletionResult notMatched() {
            return new MatchCompletionResult(false, null, null, null);
        }
    }
}

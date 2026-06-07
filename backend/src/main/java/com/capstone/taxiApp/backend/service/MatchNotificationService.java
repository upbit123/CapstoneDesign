package com.capstone.taxiApp.backend.service;

import com.capstone.taxiApp.backend.entity.AppUser;
import com.capstone.taxiApp.backend.repository.AppUserRepository;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class MatchNotificationService {

    private static final Logger logger = LoggerFactory.getLogger(MatchNotificationService.class);

    private final AppUserRepository appUserRepository;

    public MatchNotificationService(AppUserRepository appUserRepository) {
        this.appUserRepository = appUserRepository;
    }

    public void notifyMatchCompleted(
            List<Long> userIds,
            long chatRoomId,
            long matchGroupId,
            String roomTitle
    ) {
        List<AppUser> users = appUserRepository.findByUserIdIn(userIds);
        for (AppUser user : users) {
            if (user.getPushToken() == null || user.getPushToken().isBlank()) {
                continue;
            }

            Map<String, String> data = new HashMap<>();
            data.put("type", "MATCH_COMPLETED");
            data.put("title", "4인 매칭 완료");
            data.put("body", roomTitle + " 채팅방이 열렸습니다.");
            data.put("chatRoomId", String.valueOf(chatRoomId));
            data.put("matchGroupId", String.valueOf(matchGroupId));
            data.put("roomTitle", roomTitle);

            Message message = Message.builder()
                    .setToken(user.getPushToken())
                    .putAllData(data)
                    .build();

            try {
                FirebaseMessaging.getInstance().send(message);
            } catch (Exception exception) {
                logger.warn(
                        "Failed to send match notification to userId={} tokenPrefix={}",
                        user.getUserId(),
                        user.getPushToken().substring(0, Math.min(12, user.getPushToken().length())),
                        exception
                );
            }
        }
    }
}

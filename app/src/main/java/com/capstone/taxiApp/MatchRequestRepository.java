package com.capstone.taxiApp;

import androidx.annotation.NonNull;

import org.json.JSONObject;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class MatchRequestRepository {

    private static final String BACKEND_BASE_URL = "http://10.0.2.2:8080";

    @NonNull
    public MatchRequestResult createMatchRequest(
            @NonNull String idToken,
            long startZoneId,
            long targetZoneId,
            @NonNull String directionType
    ) throws Exception {
        HttpURLConnection connection = null;
        try {
            URL url = new URL(BACKEND_BASE_URL + "/api/v1/match-requests");
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setConnectTimeout(7000);
            connection.setReadTimeout(7000);
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            connection.setRequestProperty("Authorization", "Bearer " + idToken);

            JSONObject requestBody = new JSONObject();
            requestBody.put("startZoneId", startZoneId);
            requestBody.put("targetZoneId", targetZoneId);
            requestBody.put("directionType", directionType);
            requestBody.put("desiredDepartureAt", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            requestBody.put("maxWaitMinutes", 15);

            byte[] payload = requestBody.toString().getBytes(StandardCharsets.UTF_8);
            connection.getOutputStream().write(payload);

            int responseCode = connection.getResponseCode();
            InputStream stream = responseCode >= 200 && responseCode < 300
                    ? connection.getInputStream()
                    : connection.getErrorStream();
            String body = StreamUtils.readFully(stream);
            if (responseCode < 200 || responseCode >= 300) {
                String message = new JSONObject(body).optString("message", "매칭 요청 생성에 실패했습니다.");
                throw new IllegalStateException(message);
            }

            JSONObject responseJson = new JSONObject(body);
            return new MatchRequestResult(
                    responseJson.getLong("requestId"),
                    responseJson.getString("requestStatus"),
                    responseJson.optBoolean("matched", false),
                    responseJson.optLong("matchGroupId", -1L),
                    responseJson.optLong("chatRoomId", -1L),
                    responseJson.optString("roomTitle", ""),
                    responseJson.optString("message", "매칭 요청이 생성되었습니다.")
            );
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    public static final class MatchRequestResult {
        private final long requestId;
        private final String requestStatus;
        private final boolean matched;
        private final long matchGroupId;
        private final long chatRoomId;
        private final String roomTitle;
        private final String message;

        public MatchRequestResult(
                long requestId,
                @NonNull String requestStatus,
                boolean matched,
                long matchGroupId,
                long chatRoomId,
                @NonNull String roomTitle,
                @NonNull String message
        ) {
            this.requestId = requestId;
            this.requestStatus = requestStatus;
            this.matched = matched;
            this.matchGroupId = matchGroupId;
            this.chatRoomId = chatRoomId;
            this.roomTitle = roomTitle;
            this.message = message;
        }

        public long requestId() {
            return requestId;
        }

        @NonNull
        public String requestStatus() {
            return requestStatus;
        }

        public boolean matched() {
            return matched;
        }

        public long matchGroupId() {
            return matchGroupId;
        }

        public long chatRoomId() {
            return chatRoomId;
        }

        @NonNull
        public String roomTitle() {
            return roomTitle;
        }

        @NonNull
        public String message() {
            return message;
        }
    }
}

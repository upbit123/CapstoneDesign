package com.capstone.taxiApp;

import androidx.annotation.NonNull;

import org.json.JSONObject;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class UserProfileRepository {

    private static final String BACKEND_BASE_URL = "http://10.0.2.2:8080";

    @NonNull
    public UserProfileResult fetchMyProfile(@NonNull String idToken) throws Exception {
        HttpURLConnection connection = null;
        try {
            URL url = new URL(BACKEND_BASE_URL + "/api/v1/users/me");
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(7000);
            connection.setReadTimeout(7000);
            connection.setRequestProperty("Authorization", "Bearer " + idToken);

            return parseProfileResponse(connection);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    @NonNull
    public UserProfileResult updateMyProfile(
            @NonNull String idToken,
            @NonNull String name,
            @NonNull String nickname,
            @NonNull String phone
    ) throws Exception {
        HttpURLConnection connection = null;
        try {
            URL url = new URL(BACKEND_BASE_URL + "/api/v1/users/me/profile");
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("PUT");
            connection.setConnectTimeout(7000);
            connection.setReadTimeout(7000);
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            connection.setRequestProperty("Authorization", "Bearer " + idToken);

            JSONObject requestBody = new JSONObject();
            requestBody.put("name", name);
            requestBody.put("nickname", nickname);
            requestBody.put("phone", phone);

            byte[] payload = requestBody.toString().getBytes(StandardCharsets.UTF_8);
            connection.getOutputStream().write(payload);

            return parseProfileResponse(connection);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    @NonNull
    private UserProfileResult parseProfileResponse(@NonNull HttpURLConnection connection) throws Exception {
        int responseCode = connection.getResponseCode();
        InputStream stream = responseCode >= 200 && responseCode < 300
                ? connection.getInputStream()
                : connection.getErrorStream();
        String body = StreamUtils.readFully(stream);
        if (responseCode < 200 || responseCode >= 300) {
            String message = new JSONObject(body).optString("message", "프로필 요청에 실패했습니다.");
            throw new IllegalStateException(message);
        }

        JSONObject responseJson = new JSONObject(body);
        return new UserProfileResult(
                responseJson.getLong("userId"),
                responseJson.getLong("universityId"),
                responseJson.getString("email"),
                responseJson.getString("name"),
                responseJson.optString("nickname", ""),
                responseJson.optString("phone", ""),
                responseJson.optBoolean("profileCompleted", false)
        );
    }

    public static final class UserProfileResult {
        private final long userId;
        private final long universityId;
        private final String email;
        private final String name;
        private final String nickname;
        private final String phone;
        private final boolean profileCompleted;

        public UserProfileResult(
                long userId,
                long universityId,
                @NonNull String email,
                @NonNull String name,
                @NonNull String nickname,
                @NonNull String phone,
                boolean profileCompleted
        ) {
            this.userId = userId;
            this.universityId = universityId;
            this.email = email;
            this.name = name;
            this.nickname = nickname;
            this.phone = phone;
            this.profileCompleted = profileCompleted;
        }

        public long userId() {
            return userId;
        }

        public long universityId() {
            return universityId;
        }

        @NonNull
        public String email() {
            return email;
        }

        @NonNull
        public String name() {
            return name;
        }

        @NonNull
        public String nickname() {
            return nickname;
        }

        @NonNull
        public String phone() {
            return phone;
        }

        public boolean profileCompleted() {
            return profileCompleted;
        }
    }
}

package com.capstone.taxiApp;

import androidx.annotation.NonNull;

import org.json.JSONObject;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

// Firebase 로그인 이후 백엔드와 사용자 연동을 수행하는 간단한 API 클라이언트.
public class BackendAuthRepository {

    private static final String BACKEND_BASE_URL = "http://10.0.2.2:8080";

    @NonNull
    public BackendLoginResult loginWithFirebase(
            @NonNull String idToken,
            @NonNull String studentId,
            @NonNull String displayName
    ) throws Exception {
        HttpURLConnection connection = null;
        try {
            URL url = new URL(BACKEND_BASE_URL + "/api/v1/auth/firebase-login");
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setConnectTimeout(7000);
            connection.setReadTimeout(7000);
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");

            JSONObject requestBody = new JSONObject();
            requestBody.put("idToken", idToken);
            requestBody.put("studentId", studentId);
            requestBody.put("displayName", displayName);

            byte[] payload = requestBody.toString().getBytes(StandardCharsets.UTF_8);
            connection.getOutputStream().write(payload);

            int responseCode = connection.getResponseCode();
            InputStream stream = responseCode >= 200 && responseCode < 300
                    ? connection.getInputStream()
                    : connection.getErrorStream();
            String body = StreamUtils.readFully(stream);
            if (responseCode < 200 || responseCode >= 300) {
                String message = new JSONObject(body).optString("message", "백엔드 로그인에 실패했습니다.");
                throw new IllegalStateException(message);
            }

            JSONObject responseJson = new JSONObject(body);
            return new BackendLoginResult(
                    responseJson.getLong("userId"),
                    responseJson.getLong("universityId"),
                    responseJson.getString("email"),
                    responseJson.getString("name"),
                    responseJson.optString("nickname", ""),
                    responseJson.optString("phone", ""),
                    responseJson.optBoolean("profileCompleted", false),
                    responseJson.optString("message", "로그인 성공")
            );
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    @NonNull
    public String lookupLoginEmailByStudentId(@NonNull String studentId) throws Exception {
        HttpURLConnection connection = null;
        try {
            URL url = new URL(BACKEND_BASE_URL + "/api/v1/public/auth/student-login-email");
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setConnectTimeout(7000);
            connection.setReadTimeout(7000);
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");

            JSONObject requestBody = new JSONObject();
            requestBody.put("studentId", studentId);
            byte[] payload = requestBody.toString().getBytes(StandardCharsets.UTF_8);
            connection.getOutputStream().write(payload);

            int responseCode = connection.getResponseCode();
            InputStream stream = responseCode >= 200 && responseCode < 300
                    ? connection.getInputStream()
                    : connection.getErrorStream();
            String body = StreamUtils.readFully(stream);
            if (responseCode < 200 || responseCode >= 300) {
                String message = new JSONObject(body).optString("message", "로그인용 이메일 조회에 실패했습니다.");
                throw new IllegalStateException(message);
            }

            return new JSONObject(body).getString("email");
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    public static final class BackendLoginResult {
        private final long userId;
        private final long universityId;
        private final String email;
        private final String name;
        private final String nickname;
        private final String phone;
        private final boolean profileCompleted;
        private final String message;

        public BackendLoginResult(
                long userId,
                long universityId,
                @NonNull String email,
                @NonNull String name,
                @NonNull String nickname,
                @NonNull String phone,
                boolean profileCompleted,
                @NonNull String message
        ) {
            this.userId = userId;
            this.universityId = universityId;
            this.email = email;
            this.name = name;
            this.nickname = nickname;
            this.phone = phone;
            this.profileCompleted = profileCompleted;
            this.message = message;
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

        @NonNull
        public String message() {
            return message;
        }
    }
}

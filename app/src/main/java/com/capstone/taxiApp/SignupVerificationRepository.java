package com.capstone.taxiApp;

import androidx.annotation.NonNull;

import org.json.JSONObject;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class SignupVerificationRepository {

    private static final String BACKEND_BASE_URL = "http://10.0.2.2:8080";

    public void sendVerificationCode(
            @NonNull String studentId,
            @NonNull String email
    ) throws Exception {
        JSONObject body = new JSONObject();
        body.put("studentId", studentId);
        body.put("email", email);
        post("/api/v1/public/signup-verifications/send-code", body);
    }

    public void verifyCode(
            @NonNull String studentId,
            @NonNull String email,
            @NonNull String verificationCode
    ) throws Exception {
        JSONObject body = new JSONObject();
        body.put("studentId", studentId);
        body.put("email", email);
        body.put("verificationCode", verificationCode);
        post("/api/v1/public/signup-verifications/verify-code", body);
    }

    private void post(@NonNull String path, @NonNull JSONObject requestBody) throws Exception {
        HttpURLConnection connection = null;
        try {
            URL url = new URL(BACKEND_BASE_URL + path);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setConnectTimeout(7000);
            connection.setReadTimeout(7000);
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");

            byte[] payload = requestBody.toString().getBytes(StandardCharsets.UTF_8);
            connection.getOutputStream().write(payload);

            int responseCode = connection.getResponseCode();
            InputStream stream = responseCode >= 200 && responseCode < 300
                    ? connection.getInputStream()
                    : connection.getErrorStream();
            String body = StreamUtils.readFully(stream);
            if (responseCode < 200 || responseCode >= 300) {
                String message = new JSONObject(body).optString("message", "학생 인증 요청에 실패했습니다.");
                throw new IllegalStateException(message);
            }
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }
}

package com.capstone.taxiApp;

import androidx.annotation.NonNull;

import org.json.JSONObject;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class PushTokenRepository {

    private static final String BACKEND_BASE_URL = "http://10.0.2.2:8080";

    public void updatePushToken(@NonNull String idToken, @NonNull String pushToken) throws Exception {
        HttpURLConnection connection = null;
        try {
            URL url = new URL(BACKEND_BASE_URL + "/api/v1/me/push-token");
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setConnectTimeout(7000);
            connection.setReadTimeout(7000);
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            connection.setRequestProperty("Authorization", "Bearer " + idToken);

            JSONObject requestBody = new JSONObject();
            requestBody.put("pushToken", pushToken);
            byte[] payload = requestBody.toString().getBytes(StandardCharsets.UTF_8);
            connection.getOutputStream().write(payload);

            int responseCode = connection.getResponseCode();
            InputStream stream = responseCode >= 200 && responseCode < 300
                    ? connection.getInputStream()
                    : connection.getErrorStream();
            String body = StreamUtils.readFully(stream);
            if (responseCode < 200 || responseCode >= 300) {
                String message = new JSONObject(body).optString("message", "푸시 토큰 저장에 실패했습니다.");
                throw new IllegalStateException(message);
            }
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }
}

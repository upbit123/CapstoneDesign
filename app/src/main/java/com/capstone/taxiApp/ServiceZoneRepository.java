package com.capstone.taxiApp;

import androidx.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class ServiceZoneRepository {

    private static final String BACKEND_BASE_URL = "http://10.0.2.2:8080";

    @NonNull
    public List<ServiceZoneItem> fetchMyServiceZones(@NonNull String idToken) throws Exception {
        HttpURLConnection connection = null;
        try {
            URL url = new URL(BACKEND_BASE_URL + "/api/v1/service-zones/me");
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(7000);
            connection.setReadTimeout(7000);
            connection.setRequestProperty("Authorization", "Bearer " + idToken);

            int responseCode = connection.getResponseCode();
            InputStream stream = responseCode >= 200 && responseCode < 300
                    ? connection.getInputStream()
                    : connection.getErrorStream();
            String body = StreamUtils.readFully(stream);
            if (responseCode < 200 || responseCode >= 300) {
                String message = new JSONObject(body).optString("message", "서비스 존 목록 조회에 실패했습니다.");
                throw new IllegalStateException(message);
            }

            JSONArray array = new JSONArray(body);
            List<ServiceZoneItem> items = new ArrayList<>();
            for (int i = 0; i < array.length(); i++) {
                JSONObject item = array.getJSONObject(i);
                items.add(new ServiceZoneItem(
                        item.getLong("zoneId"),
                        item.getString("zoneName"),
                        item.optString("zoneType", "CUSTOM")
                ));
            }
            return items;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    public static final class ServiceZoneItem {
        private final long zoneId;
        private final String zoneName;
        private final String zoneType;

        public ServiceZoneItem(long zoneId, @NonNull String zoneName, @NonNull String zoneType) {
            this.zoneId = zoneId;
            this.zoneName = zoneName;
            this.zoneType = zoneType;
        }

        public long zoneId() {
            return zoneId;
        }

        @NonNull
        public String zoneName() {
            return zoneName;
        }

        @NonNull
        public String zoneType() {
            return zoneType;
        }
    }
}

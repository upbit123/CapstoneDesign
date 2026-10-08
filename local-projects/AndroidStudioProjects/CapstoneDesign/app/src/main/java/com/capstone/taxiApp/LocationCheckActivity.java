package com.capstone.taxiApp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LocationCheckActivity extends AppCompatActivity implements OnMapReadyCallback {

    public static final String EXTRA_TARGET_ZONE_ID = "target_zone_id";
    public static final String EXTRA_TARGET_ZONE_NAME = "target_zone_name";
    public static final String EXTRA_DIRECTION_TYPE = "direction_type";

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    private static final String TAG = "GPS_AUTH";
    private static final String BACKEND_BASE_URL = "http://10.0.2.2:8080";

    private GoogleMap googleMap;
    private GpsTracker gpsTracker;
    private TextView statusText;
    private TextView routeSummaryText;
    private List<VerificationPolicy> verificationPolicies = new ArrayList<>();
    private SessionManager sessionManager;
    private MatchRequestRepository matchRequestRepository;
    private long targetZoneId;
    private String targetZoneName;
    private String directionType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_location_check);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        gpsTracker = new GpsTracker(this);
        sessionManager = new SessionManager(this);
        matchRequestRepository = new MatchRequestRepository();
        statusText = findViewById(R.id.tvStatus);
        routeSummaryText = findViewById(R.id.tvRouteSummary);
        Button verifyButton = findViewById(R.id.btnVerifyLocation);
        verifyButton.setOnClickListener(v -> verifyCurrentLocation());

        targetZoneId = getIntent().getLongExtra(EXTRA_TARGET_ZONE_ID, -1L);
        targetZoneName = getIntent().getStringExtra(EXTRA_TARGET_ZONE_NAME);
        directionType = getIntent().getStringExtra(EXTRA_DIRECTION_TYPE);
        routeSummaryText.setText(buildRouteSummary());

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        fetchVerificationPoliciesFromServer();
        checkAndRequestLocationPermission();
    }

    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        googleMap = map;
        enableMyLocationLayer();
        renderVerificationZone(null, null);
    }

    private void checkAndRequestLocationPermission() {
        boolean fineGranted = ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED;
        boolean coarseGranted = ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED;

        if (fineGranted || coarseGranted) {
            statusText.setText(buildReadyMessage());
            enableMyLocationLayer();
            return;
        }

        ActivityCompat.requestPermissions(
                this,
                new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                },
                LOCATION_PERMISSION_REQUEST_CODE
        );
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode != LOCATION_PERMISSION_REQUEST_CODE) {
            return;
        }

        boolean granted = false;
        for (int result : grantResults) {
            if (result == PackageManager.PERMISSION_GRANTED) {
                granted = true;
                break;
            }
        }

        if (granted) {
            statusText.setText(buildReadyMessage());
            enableMyLocationLayer();
        } else {
            statusText.setText("위치 권한이 없어 인증을 진행할 수 없습니다.");
        }
    }

    private void enableMyLocationLayer() {
        if (googleMap == null) {
            return;
        }
        boolean fineGranted = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        boolean coarseGranted = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        if (!fineGranted && !coarseGranted) {
            return;
        }
        googleMap.setMyLocationEnabled(true);
    }

    private void verifyCurrentLocation() {
        if (!sessionManager.hasActiveSession()) {
            statusText.setText("로그인 후 다시 시도해 주세요.");
            Toast.makeText(this, "백엔드 로그인 세션이 없습니다.", Toast.LENGTH_SHORT).show();
            return;
        }
        statusText.setText("정밀 GPS 현재 위치를 조회 중입니다...");
        gpsTracker.getCurrentLocation(new GpsTracker.OnLocationReceivedListener() {
            @Override
            public void onReceived(@NonNull GpsTracker.LocationSnapshot snapshot) {
                FirebaseTokenHelper.fetchIdToken(new FirebaseTokenHelper.TokenCallback() {
                    @Override
                    public void onSuccess(@NonNull String idToken) {
                        statusText.setText("정밀 GPS 위치를 서버에 저장/인증 중입니다...");
                        verifyLocationWithServer(snapshot, idToken);
                    }

                    @Override
                    public void onFailure(@NonNull String message) {
                        statusText.setText("토큰 발급 실패: " + message);
                        Toast.makeText(LocationCheckActivity.this, message, Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onFailed(@NonNull String message) {
                statusText.setText("정밀 GPS 위치 조회 실패: " + message);
                Toast.makeText(LocationCheckActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void verifyLocationWithServer(
            @NonNull GpsTracker.LocationSnapshot snapshot,
            @NonNull String idToken
    ) {
        new Thread(() -> {
            HttpURLConnection connection = null;
            try {
                URL url = new URL(BACKEND_BASE_URL + "/api/v1/location-verification/verify");
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setConnectTimeout(7000);
                connection.setReadTimeout(7000);
                connection.setDoOutput(true);
                connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                connection.setRequestProperty("Authorization", "Bearer " + idToken);

                JSONObject requestBody = new JSONObject();
                requestBody.put("requestPurpose", "GPS_PRECISION_VERIFY");
                requestBody.put("latitude", snapshot.getLatitude());
                requestBody.put("longitude", snapshot.getLongitude());
                requestBody.put("accuracyMeters", snapshot.getAccuracyMeters());
                requestBody.put("capturedAtEpochMillis", snapshot.getCapturedAtEpochMillis());

                byte[] payload = requestBody.toString().getBytes(StandardCharsets.UTF_8);
                connection.getOutputStream().write(payload);

                int responseCode = connection.getResponseCode();
                InputStream stream = responseCode >= 200 && responseCode < 300
                        ? connection.getInputStream()
                        : connection.getErrorStream();
                String body = readStream(stream);
                if (responseCode < 200 || responseCode >= 300) {
                    throw new IllegalStateException("위치 인증 실패 (HTTP " + responseCode + "): " + body);
                }

                VerificationOutcome outcome = parseVerificationOutcomeFromServer(body);
                runOnUiThread(() -> {
                    renderVerificationResult(snapshot, outcome, idToken);
                    logVerificationAudit(snapshot, outcome);
                });
            } catch (Exception e) {
                Log.e(TAG, "서버 기반 위치 인증 실패", e);
                VerificationOutcome fallbackOutcome = verifyAgainstPolicies(snapshot, verificationPolicies);
                runOnUiThread(() -> {
                    renderVerificationResult(snapshot, fallbackOutcome, null);
                    logVerificationAudit(snapshot, fallbackOutcome);
                    Toast.makeText(
                            LocationCheckActivity.this,
                            "서버 인증에 실패해 로컬 기준으로 판정했습니다.",
                            Toast.LENGTH_SHORT
                    ).show();
                });
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        }).start();
    }

    private void fetchVerificationPoliciesFromServer() {
        if (!sessionManager.hasActiveSession()) {
            statusText.setText("로그인 후 인증 구역을 불러올 수 있습니다.");
            return;
        }
        FirebaseTokenHelper.fetchIdToken(new FirebaseTokenHelper.TokenCallback() {
            @Override
            public void onSuccess(@NonNull String idToken) {
                new Thread(() -> {
                    HttpURLConnection connection = null;
                    try {
                        URL url = new URL(BACKEND_BASE_URL + "/api/v1/location-verification/policies/me");
                        connection = (HttpURLConnection) url.openConnection();
                        connection.setRequestMethod("GET");
                        connection.setConnectTimeout(7000);
                        connection.setReadTimeout(7000);
                        connection.setRequestProperty("Authorization", "Bearer " + idToken);

                        int responseCode = connection.getResponseCode();
                        InputStream stream = responseCode >= 200 && responseCode < 300
                                ? connection.getInputStream()
                                : connection.getErrorStream();
                        String body = readStream(stream);
                        if (responseCode < 200 || responseCode >= 300) {
                            throw new IllegalStateException("정책 조회 실패 (HTTP " + responseCode + ")");
                        }

                        List<VerificationPolicy> loadedPolicies = parsePolicies(body);
                        runOnUiThread(() -> {
                            verificationPolicies = loadedPolicies;
                            statusText.setText(buildReadyMessage());
                            renderVerificationZone(null, null);
                        });
                    } catch (Exception e) {
                        Log.e(TAG, "인증 구역 로딩 실패", e);
                        runOnUiThread(() -> {
                            verificationPolicies = Collections.emptyList();
                            statusText.setText("인증 구역 로딩 실패: 서버 연결을 확인하세요.");
                            Toast.makeText(LocationCheckActivity.this, "인증 구역을 불러오지 못했습니다.", Toast.LENGTH_SHORT).show();
                        });
                    } finally {
                        if (connection != null) {
                            connection.disconnect();
                        }
                    }
                }).start();
            }

            @Override
            public void onFailure(@NonNull String message) {
                verificationPolicies = Collections.emptyList();
                statusText.setText("토큰 발급 실패: " + message);
                Toast.makeText(LocationCheckActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @NonNull
    private VerificationOutcome parseVerificationOutcomeFromServer(@NonNull String rawJson) throws Exception {
        JSONObject json = new JSONObject(rawJson);
        String statusCode = json.optString("verificationStatus", "OUT_OF_RANGE");
        boolean approved = json.optBoolean("approved", false);
        long matchedZoneId = json.optLong("matchedZoneId", -1L);
        float distanceMeters = (float) json.optDouble("distanceToCenterMeters", -1d);
        String failureReason = json.isNull("failureReason") ? "" : json.optString("failureReason", "");
        long verificationId = json.optLong("verificationId", -1L);
        boolean hasSavedLatitude = !json.isNull("savedLatitude");
        boolean hasSavedLongitude = !json.isNull("savedLongitude");
        double savedLatitude = hasSavedLatitude ? json.optDouble("savedLatitude", Double.NaN) : Double.NaN;
        double savedLongitude = hasSavedLongitude ? json.optDouble("savedLongitude", Double.NaN) : Double.NaN;

        VerificationPolicy matchedPolicy = null;
        for (VerificationPolicy policy : verificationPolicies) {
            if (policy.zoneId == matchedZoneId) {
                matchedPolicy = policy;
                break;
            }
        }

        String message = approved
                ? "위치 인증 성공"
                : (failureReason.isEmpty() ? "위치 인증 실패" : failureReason);

        return new VerificationOutcome(
                approved,
                matchedPolicy,
                statusCode,
                message,
                distanceMeters,
                verificationId,
                savedLatitude,
                savedLongitude
        );
    }

    @NonNull
    private VerificationOutcome verifyAgainstPolicies(
            @NonNull GpsTracker.LocationSnapshot snapshot,
            @NonNull List<VerificationPolicy> policies
    ) {
        if (policies.isEmpty()) {
            return new VerificationOutcome(
                    false,
                    null,
                    "NO_POLICY",
                    "인증 구역 데이터가 없습니다.",
                    -1f,
                    -1L,
                    snapshot.getLatitude(),
                    snapshot.getLongitude()
            );
        }

        VerificationPolicy matchedPolicy = null;
        float nearestDistance = Float.MAX_VALUE;
        float matchedDistance = Float.MAX_VALUE;
        for (VerificationPolicy policy : policies) {
            float distance = distanceMeters(
                    snapshot.getLatitude(),
                    snapshot.getLongitude(),
                    policy.centerLatitude,
                    policy.centerLongitude
            );
            if (distance < nearestDistance) {
                nearestDistance = distance;
            }
            if (distance <= policy.allowedRadiusMeters) {
                matchedPolicy = policy;
                matchedDistance = distance;
                break;
            }
        }

        if (matchedPolicy == null) {
            return new VerificationOutcome(
                    false,
                    null,
                    "OUT_OF_RANGE",
                    "허용 원형 구역 외부",
                    nearestDistance,
                    -1L,
                    snapshot.getLatitude(),
                    snapshot.getLongitude()
            );
        }

        if (snapshot.getAccuracyMeters() > matchedPolicy.maxAcceptedAccuracyMeters) {
            return new VerificationOutcome(
                    false,
                    matchedPolicy,
                    "LOW_ACCURACY",
                    "허용 정확도 초과",
                    matchedDistance,
                    -1L,
                    snapshot.getLatitude(),
                    snapshot.getLongitude()
            );
        }

        return new VerificationOutcome(
                true,
                matchedPolicy,
                "APPROVED",
                "위치 인증 성공",
                matchedDistance,
                -1L,
                snapshot.getLatitude(),
                snapshot.getLongitude()
        );
    }

    private void renderVerificationResult(
            @NonNull GpsTracker.LocationSnapshot snapshot,
            @NonNull VerificationOutcome outcome,
            String idToken
    ) {
        String zoneName = outcome.matchedPolicy != null ? outcome.matchedPolicy.zoneName : "없음";
        statusText.setText(
                outcome.message
                        + "\n상태: " + outcome.statusCode
                        + "\n구역: " + zoneName
                        + "\n거리: " + String.format("%.1f m", outcome.distanceMeters)
                        + "\n정확도: " + String.format("%.1f m", snapshot.getAccuracyMeters())
        );

        renderVerificationZone(snapshot, outcome);
        if (outcome.hasSavedLocation()) {
            renderSavedLocationOnMap(outcome.savedLatitude, outcome.savedLongitude);
        }

        if (outcome.approved) {
            if (idToken == null || idToken.isBlank()) {
                Toast.makeText(this, "매칭 요청 토큰이 없어 대기 화면으로 이동할 수 없습니다.", Toast.LENGTH_SHORT).show();
                return;
            }
            if (outcome.matchedPolicy == null || outcome.matchedPolicy.zoneId <= 0L) {
                Toast.makeText(this, "인증된 출발 구역 정보를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show();
                return;
            }
            if (targetZoneId <= 0L || directionType == null || directionType.isBlank()) {
                Toast.makeText(this, "목적지 정보가 올바르지 않습니다.", Toast.LENGTH_SHORT).show();
                return;
            }

            createMatchRequestAndOpenWaiting(
                    idToken,
                    outcome.matchedPolicy.zoneId,
                    zoneName
            );
        }
    }

    private void createMatchRequestAndOpenWaiting(
            @NonNull String idToken,
            long startZoneId,
            @NonNull String startZoneName
    ) {
        statusText.setText("매칭 요청을 생성하는 중입니다...");
        new Thread(() -> {
            try {
                MatchRequestRepository.MatchRequestResult result = matchRequestRepository.createMatchRequest(
                        idToken,
                        startZoneId,
                        targetZoneId,
                        directionType
                );

                runOnUiThread(() -> {
                    Toast.makeText(this, result.message(), Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(this, MatchingWaitActivity.class);
                    intent.putExtra(MatchingWaitActivity.EXTRA_START_ZONE_NAME, startZoneName);
                    intent.putExtra(MatchingWaitActivity.EXTRA_TARGET_ZONE_NAME, targetZoneName);
                    intent.putExtra(MatchingWaitActivity.EXTRA_DIRECTION_TYPE, directionType);
                    intent.putExtra(MatchingWaitActivity.EXTRA_REQUEST_ID, result.requestId());
                    intent.putExtra(MatchingWaitActivity.EXTRA_REQUEST_STATUS, result.requestStatus());
                    intent.putExtra(MatchingWaitActivity.EXTRA_MATCHED, result.matched());
                    intent.putExtra(MatchingWaitActivity.EXTRA_CHAT_ROOM_ID, result.chatRoomId());
                    intent.putExtra(MatchingWaitActivity.EXTRA_ROOM_TITLE, result.roomTitle());

                    if (result.matched() && result.chatRoomId() > 0L) {
                        SessionManager sessionManager = new SessionManager(this);
                        Intent chatIntent = new Intent(this, com.capstone.taxiApp.chat.ChatActivity.class);
                        chatIntent.putExtra(com.capstone.taxiApp.chat.ChatActivity.EXTRA_ROOM_ID, "chat-room-" + result.chatRoomId());
                        chatIntent.putExtra(
                                com.capstone.taxiApp.chat.ChatActivity.EXTRA_ROOM_TITLE,
                                result.roomTitle().isBlank() ? startZoneName + " -> " + targetZoneName : result.roomTitle()
                        );
                        chatIntent.putExtra(com.capstone.taxiApp.chat.ChatActivity.EXTRA_SENDER_USER_ID, String.valueOf(sessionManager.getUserId()));
                        chatIntent.putExtra(com.capstone.taxiApp.chat.ChatActivity.EXTRA_SENDER_NAME, sessionManager.getDisplayName());
                        startActivity(chatIntent);
                    } else {
                        startActivity(intent);
                    }
                });
            } catch (Exception exception) {
                runOnUiThread(() -> Toast.makeText(
                        this,
                        "매칭 요청 실패: " + exception.getMessage(),
                        Toast.LENGTH_LONG
                ).show());
            }
        }).start();
    }

    private void renderSavedLocationOnMap(double latitude, double longitude) {
        if (googleMap == null) {
            return;
        }

        LatLng savedLocation = new LatLng(latitude, longitude);
        googleMap.addMarker(
                new MarkerOptions()
                        .position(savedLocation)
                        .title("저장된 사용자 위치")
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE))
        );
    }

    private void renderVerificationZone(
            GpsTracker.LocationSnapshot snapshot,
            VerificationOutcome outcome
    ) {
        if (googleMap == null) {
            return;
        }

        googleMap.clear();
        LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();
        boolean hasBounds = false;

        for (VerificationPolicy policy : verificationPolicies) {
            LatLng center = new LatLng(policy.centerLatitude, policy.centerLongitude);
            boundsBuilder.include(center);
            hasBounds = true;
            googleMap.addCircle(new CircleOptions()
                    .center(center)
                    .radius(policy.allowedRadiusMeters)
                    .strokeWidth(4f)
                    .strokeColor(0xFF0C356A)
                    .fillColor(0x220C356A));
            googleMap.addMarker(new MarkerOptions().position(center).title(policy.zoneName));
        }

        if (snapshot != null) {
            LatLng userLocation = new LatLng(snapshot.getLatitude(), snapshot.getLongitude());
            boundsBuilder.include(userLocation);
            hasBounds = true;
            googleMap.addMarker(
                    new MarkerOptions()
                            .position(userLocation)
                            .title(outcome != null && outcome.approved ? "인증 성공 위치" : "인증 실패 위치")
                            .icon(BitmapDescriptorFactory.defaultMarker(
                                    outcome != null && outcome.approved
                                            ? BitmapDescriptorFactory.HUE_GREEN
                                            : BitmapDescriptorFactory.HUE_RED
                            ))
            );
        }

        if (!hasBounds) {
            return;
        }

        try {
            googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(boundsBuilder.build(), 120));
        } catch (IllegalStateException ignored) {
            if (snapshot != null) {
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                        new LatLng(snapshot.getLatitude(), snapshot.getLongitude()), 15f
                ));
            } else if (!verificationPolicies.isEmpty()) {
                VerificationPolicy first = verificationPolicies.get(0);
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                        new LatLng(first.centerLatitude, first.centerLongitude), 14f
                ));
            }
        }
    }

    @NonNull
    private String buildReadyMessage() {
        if (verificationPolicies.isEmpty()) {
            return "권한 허용됨: 인증 구역 데이터를 불러오는 중입니다.";
        }
        return "권한 허용됨: 인증 버튼을 눌러 위치를 확인하세요.\n구역 목록: " + buildZoneSummary();
    }

    @NonNull
    private String buildRouteSummary() {
        if (targetZoneId <= 0L || targetZoneName == null || targetZoneName.isBlank()) {
            return "선택된 목적지 정보가 없습니다.";
        }
        return "선택 목적지: " + targetZoneName + "\n방향: " + (directionType == null ? "미정" : directionType);
    }

    private void logVerificationAudit(
            @NonNull GpsTracker.LocationSnapshot snapshot,
            @NonNull VerificationOutcome outcome
    ) {
        String matchedPolicyId = outcome.matchedPolicy != null ? String.valueOf(outcome.matchedPolicy.policyId) : "null";
        String matchedZoneId = outcome.matchedPolicy != null ? String.valueOf(outcome.matchedPolicy.zoneId) : "null";
        String matchedZoneName = outcome.matchedPolicy != null ? outcome.matchedPolicy.zoneName : "null";
        String auditPayload = "verificationPayload={"
                + "verificationId=" + outcome.verificationId
                + ", matchedPolicyId=" + matchedPolicyId
                + ", universityId=1"
                + ", matchedZoneId=" + matchedZoneId
                + ", matchedZoneName='" + matchedZoneName + '\''
                + ", latitude=" + snapshot.getLatitude()
                + ", longitude=" + snapshot.getLongitude()
                + ", accuracyMeters=" + snapshot.getAccuracyMeters()
                + ", capturedAtEpochMillis=" + snapshot.getCapturedAtEpochMillis()
                + ", verificationStatus='" + outcome.statusCode + '\''
                + ", approved=" + outcome.approved
                + ", distanceToCenterMeters=" + outcome.distanceMeters
                + ", checkedPolicyCount=" + verificationPolicies.size()
                + '}';
        Log.i(TAG, auditPayload);
    }

    @NonNull
    private String buildZoneSummary() {
        if (verificationPolicies.isEmpty()) {
            return "없음";
        }
        StringBuilder summary = new StringBuilder();
        for (int index = 0; index < verificationPolicies.size(); index++) {
            if (index > 0) {
                summary.append(", ");
            }
            summary.append(verificationPolicies.get(index).zoneName);
        }
        return summary.toString();
    }

    @NonNull
    private List<VerificationPolicy> parsePolicies(@NonNull String rawJson) throws Exception {
        JSONArray array = new JSONArray(rawJson);
        List<VerificationPolicy> result = new ArrayList<>();

        for (int i = 0; i < array.length(); i++) {
            JSONObject item = array.getJSONObject(i);
            if (!item.has("centerLatitude") || !item.has("centerLongitude") || !item.has("allowedRadiusMeters")) {
                continue;
            }

            result.add(new VerificationPolicy(
                    item.getLong("policyId"),
                    item.getLong("universityId"),
                    item.optLong("zoneId", -1L),
                    item.optString("policyName", "인증 구역"),
                    (float) item.optDouble("maxAccuracyMeters", 60d),
                    item.getDouble("centerLatitude"),
                    item.getDouble("centerLongitude"),
                    (float) item.getDouble("allowedRadiusMeters")
            ));
        }

        return result;
    }

    @NonNull
    private String readStream(InputStream stream) throws Exception {
        return StreamUtils.readFully(stream);
    }

    private float distanceMeters(double lat1, double lng1, double lat2, double lng2) {
        float[] result = new float[1];
        android.location.Location.distanceBetween(lat1, lng1, lat2, lng2, result);
        return result[0];
    }

    private static final class VerificationPolicy {
        private final long policyId;
        private final long universityId;
        private final long zoneId;
        private final String zoneName;
        private final double centerLatitude;
        private final double centerLongitude;
        private final float allowedRadiusMeters;
        private final float maxAcceptedAccuracyMeters;

        private VerificationPolicy(
                long policyId,
                long universityId,
                long zoneId,
                @NonNull String zoneName,
                float maxAcceptedAccuracyMeters,
                double centerLatitude,
                double centerLongitude,
                float allowedRadiusMeters
        ) {
            this.policyId = policyId;
            this.universityId = universityId;
            this.zoneId = zoneId;
            this.zoneName = zoneName;
            this.centerLatitude = centerLatitude;
            this.centerLongitude = centerLongitude;
            this.allowedRadiusMeters = allowedRadiusMeters;
            this.maxAcceptedAccuracyMeters = maxAcceptedAccuracyMeters;
        }
    }

    private static final class VerificationOutcome {
        private final boolean approved;
        private final VerificationPolicy matchedPolicy;
        private final String statusCode;
        private final String message;
        private final float distanceMeters;
        private final long verificationId;
        private final double savedLatitude;
        private final double savedLongitude;

        private VerificationOutcome(
                boolean approved,
                VerificationPolicy matchedPolicy,
                @NonNull String statusCode,
                @NonNull String message,
                float distanceMeters,
                long verificationId,
                double savedLatitude,
                double savedLongitude
        ) {
            this.approved = approved;
            this.matchedPolicy = matchedPolicy;
            this.statusCode = statusCode;
            this.message = message;
            this.distanceMeters = distanceMeters;
            this.verificationId = verificationId;
            this.savedLatitude = savedLatitude;
            this.savedLongitude = savedLongitude;
        }

        private boolean hasSavedLocation() {
            return !Double.isNaN(savedLatitude) && !Double.isNaN(savedLongitude);
        }
    }
}

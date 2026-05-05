package com.capstone.taxiApp;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
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

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {
    // 위치 권한 요청 결과를 구분하기 위한 요청 코드
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    private static final String TAG = "GPS_AUTH";
    private static final long DEFAULT_USER_ID = 1L;
    // 샘플 계정 기준 학교 ID다. 실제 운영에서는 로그인 사용자 정보로 대체한다.
    private static final long DEFAULT_UNIVERSITY_ID = 1L;
    // 에뮬레이터에서 로컬 백엔드 접근 시 10.0.2.2를 사용한다.
    private static final String BACKEND_BASE_URL = "http://10.0.2.2:8080";

    private GoogleMap googleMap;
    private GpsTracker gpsTracker;
    private TextView statusText;
    // 현재 화면에서 사용할 전체 인증 구역(원형) 목록이다.
    private List<VerificationPolicy> verificationPolicies;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // 현재 위치 조회용 헬퍼 객체 초기화
        gpsTracker = new GpsTracker(this);
        statusText = findViewById(R.id.tvStatus);
        Button verifyButton = findViewById(R.id.btnVerifyLocation);
        verificationPolicies = new ArrayList<>();

        // 지도 프래그먼트를 비동기로 준비
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        // 버튼 클릭 시 현재 위치 인증 실행
        verifyButton.setOnClickListener(v -> verifyCurrentLocation());

        // 앱 시작 시 DB 기반 원형 인증 구역을 백엔드에서 로드한다.
        fetchVerificationPoliciesFromServer();

        // 앱 시작 시 위치 권한 상태 확인
        checkAndRequestLocationPermission();
    }

    private void checkAndRequestLocationPermission() {
        // FINE 또는 COARSE 중 하나라도 허용되면 위치 기능 사용 가능
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

        // 권한이 없으면 런타임 권한 요청
        ActivityCompat.requestPermissions(
                this,
                new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                },
                LOCATION_PERMISSION_REQUEST_CODE
        );
    }

    private void verifyCurrentLocation() {
        statusText.setText("정밀 GPS 현재 위치를 조회 중입니다...");

        gpsTracker.getCurrentLocation(new GpsTracker.OnLocationReceivedListener() {
            @Override
            public void onReceived(@NonNull GpsTracker.LocationSnapshot snapshot) {
                statusText.setText("정밀 GPS 위치를 서버에 저장/인증 중입니다...");
                verifyLocationWithServer(snapshot);
            }

            @Override
            public void onFailed(@NonNull String message) {
                statusText.setText("정밀 GPS 위치 조회 실패: " + message);
                Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });
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
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(savedLocation, 15f));
    }

    private void verifyLocationWithServer(@NonNull GpsTracker.LocationSnapshot snapshot) {
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

                JSONObject requestBody = new JSONObject();
                requestBody.put("userId", DEFAULT_USER_ID);
                requestBody.put("universityId", DEFAULT_UNIVERSITY_ID);
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
                    renderVerificationResult(snapshot, outcome);
                    logVerificationAudit(snapshot, outcome);
                });
            } catch (Exception e) {
                Log.e(TAG, "서버 기반 위치 인증 실패", e);
                VerificationOutcome fallbackOutcome = verifyAgainstPolicies(snapshot, verificationPolicies);
                runOnUiThread(() -> {
                    renderVerificationResult(snapshot, fallbackOutcome);
                    logVerificationAudit(snapshot, fallbackOutcome);
                    Toast.makeText(
                            MainActivity.this,
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
        double savedLatitude = hasSavedLatitude ? json.getDouble("savedLatitude") : Double.NaN;
        double savedLongitude = hasSavedLongitude ? json.getDouble("savedLongitude") : Double.NaN;

        VerificationPolicy matchedPolicy = findPolicyByZoneId(matchedZoneId);
        String message = approved
                ? "인증 성공: 서버에서 GPS 위치 인증이 완료되었습니다."
                : buildFailureMessage(statusCode, failureReason);

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
    private String buildFailureMessage(@NonNull String statusCode, @NonNull String failureReason) {
        if ("LOW_ACCURACY".equals(statusCode)) {
            return "인증 실패: GPS 정확도가 낮습니다.";
        }
        if ("OUT_OF_RANGE".equals(statusCode)) {
            return "인증 실패: 허용 원형 구역 밖에 있습니다.";
        }
        if (!failureReason.isEmpty()) {
            return "인증 실패: " + failureReason;
        }
        return "인증 실패: 서버에서 위치 인증이 거부되었습니다.";
    }

    private VerificationPolicy findPolicyByZoneId(long zoneId) {
        if (zoneId < 0 || verificationPolicies == null || verificationPolicies.isEmpty()) {
            return null;
        }
        for (VerificationPolicy policy : verificationPolicies) {
            if (policy.zoneId == zoneId) {
                return policy;
            }
        }
        return null;
    }

    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        // 지도 준비 완료 시 참조를 저장하고 내 위치 레이어 활성화 시도
        googleMap = map;
        renderVerificationZone(null, null);
        enableMyLocationLayer();
    }

    private void enableMyLocationLayer() {
        // 지도 객체가 준비되지 않았으면 종료
        if (googleMap == null) {
            return;
        }

        boolean fineGranted = ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED;
        boolean coarseGranted = ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED;

        // 권한이 있는 경우에만 파란 점(내 위치) 표시
        if (fineGranted || coarseGranted) {
            googleMap.setMyLocationEnabled(true);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode != LOCATION_PERMISSION_REQUEST_CODE) {
            return;
        }

        // 요청한 권한 중 하나라도 허용되었는지 확인
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
            // 권한 거부 시 인증 기능 사용 불가 안내
            statusText.setText("권한 거부됨: 위치 인증을 사용할 수 없습니다.");
            Toast.makeText(this, "위치 권한이 필요합니다.", Toast.LENGTH_SHORT).show();
        }
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
                    "OUT_OF_RANGE",
                    "인증 실패: 유효한 인증 구역이 없습니다.",
                    -1f,
                    -1L,
                    Double.NaN,
                    Double.NaN
            );
        }

        // 사용자 좌표가 원형 인증 구역 중 어디에 포함되는지 검사한다.
        VerificationPolicy matchedPolicy = null;
        float matchedDistance = Float.MAX_VALUE;
        for (VerificationPolicy policy : policies) {
            float[] distanceResult = new float[1];
            Location.distanceBetween(
                    snapshot.getLatitude(),
                    snapshot.getLongitude(),
                    policy.centerLatitude,
                    policy.centerLongitude,
                    distanceResult
            );

            if (distanceResult[0] <= policy.allowedRadiusMeters) {
                matchedPolicy = policy;
                matchedDistance = distanceResult[0];
                break;
            }
        }

        if (matchedPolicy == null) {
            return new VerificationOutcome(
                    false,
                    null,
                    "OUT_OF_RANGE",
                    "인증 실패: 허용 원형 구역 밖에 있습니다.",
                    -1f,
                    -1L,
                    Double.NaN,
                    Double.NaN
            );
        }

        if (snapshot.getAccuracyMeters() > matchedPolicy.maxAcceptedAccuracyMeters) {
            return new VerificationOutcome(
                    false,
                    matchedPolicy,
                    "LOW_ACCURACY",
                    "인증 실패: GPS 정확도가 낮습니다.",
                    matchedDistance,
                    -1L,
                    Double.NaN,
                    Double.NaN
            );
        }

        return new VerificationOutcome(
                true,
                matchedPolicy,
                "APPROVED",
                "인증 성공: 허용 원형 구역 안에 있습니다.",
                matchedDistance,
            -1L,
            Double.NaN,
            Double.NaN
        );
    }

    private void renderVerificationResult(
            @NonNull GpsTracker.LocationSnapshot snapshot,
            @NonNull VerificationOutcome outcome
    ) {
        // 지도 표시와 텍스트 안내를 같은 결과 기준으로 맞춘다.
        renderVerificationZone(snapshot, outcome);
        String zoneLabel = outcome.matchedPolicy != null ? outcome.matchedPolicy.zoneName : "없음";
        statusText.setText(
                outcome.message
                + "\n인증 구역: " + zoneLabel
            + "\n등록된 원형 구역 수: " + verificationPolicies.size() + "개"
            + "\n중심점 거리: " + (outcome.distanceMeters >= 0 ? Math.round(outcome.distanceMeters) + "m" : "미확인")
                        + "\n정확도: " + Math.round(snapshot.getAccuracyMeters()) + "m"
                        + "\n위치 수집 시각: " + formatLocationAge(snapshot)
                        + "\n인증 이력 ID: " + (outcome.verificationId > 0 ? outcome.verificationId : "미저장")
                        + "\n좌표: " + snapshot.getLatitude() + ", " + snapshot.getLongitude()
        );
        Toast.makeText(this, outcome.message, Toast.LENGTH_SHORT).show();
    }

    @NonNull
    private String formatLocationAge(@NonNull GpsTracker.LocationSnapshot snapshot) {
        long capturedAt = snapshot.getCapturedAtEpochMillis();
        if (capturedAt <= 0L) {
            return "알 수 없음";
        }
        long ageMillis = System.currentTimeMillis() - capturedAt;
        if (ageMillis < 0L) {
            return "방금";
        }
        long ageSeconds = ageMillis / 1000L;
        return ageSeconds + "초 전";
    }

    private void renderVerificationZone(
            GpsTracker.LocationSnapshot snapshot,
            VerificationOutcome outcome
    ) {
        if (googleMap == null) {
            return;
        }

        // 매 인증 시마다 원형 구역과 최신 사용자 위치만 보이도록 지도를 다시 그린다.
        googleMap.clear();

        // 서버 데이터가 없으면 사용자 위치만 표시하고 원형 구역 렌더링은 건너뛴다.
        if (verificationPolicies == null || verificationPolicies.isEmpty()) {
            if (snapshot != null) {
                LatLng userLocation = new LatLng(snapshot.getLatitude(), snapshot.getLongitude());
                googleMap.addMarker(
                        new MarkerOptions()
                                .position(userLocation)
                                .title("현재 위치")
                );
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 16f));
            }
            return;
        }

        LatLngBounds.Builder boundsBuilder = LatLngBounds.builder();

        for (VerificationPolicy policy : verificationPolicies) {
            boolean isMatched = outcome != null
                && outcome.matchedPolicy != null
                && outcome.matchedPolicy.policyId == policy.policyId;
            int strokeColor = isMatched ? 0xFF16A34A : 0xFF2563EB;
            int fillColor = isMatched ? 0x3322C55E : 0x223B82F6;

            LatLng center = new LatLng(policy.centerLatitude, policy.centerLongitude);
            googleMap.addCircle(
                new CircleOptions()
                    .center(center)
                    .radius(policy.allowedRadiusMeters)
                    .strokeWidth(4f)
                    .strokeColor(strokeColor)
                    .fillColor(fillColor)
            );
            googleMap.addMarker(
                new MarkerOptions()
                    .position(center)
                    .title(policy.zoneName)
            );

            boundsBuilder.include(center);
        }

        if (snapshot == null) {
            // 아직 사용자 위치가 없을 때는 등록된 원형 구역 전체가 보이도록 카메라를 맞춘다.
            googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(boundsBuilder.build(), 120));
            return;
        }

        LatLng userLocation = new LatLng(snapshot.getLatitude(), snapshot.getLongitude());
        boundsBuilder.include(userLocation);
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
        googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(boundsBuilder.build(), 120));
    }

    @NonNull
    private String buildReadyMessage() {
        if (verificationPolicies == null || verificationPolicies.isEmpty()) {
            return "권한 허용됨: 인증 구역 데이터를 불러오는 중입니다.";
        }
        return "권한 허용됨: 인증 버튼을 눌러 위치를 확인하세요."
            + "\n기준 구역: DB 기반 원형 인증 구역"
                + "\n구역 목록: " + buildZoneSummary();
    }

    private void logVerificationAudit(
            @NonNull GpsTracker.LocationSnapshot snapshot,
            @NonNull VerificationOutcome outcome
    ) {
        // 서버 응답 기반 인증 결과를 진단 목적으로 Logcat에 남긴다.
        String matchedPolicyId = outcome.matchedPolicy != null ? String.valueOf(outcome.matchedPolicy.policyId) : "null";
        String matchedZoneId = outcome.matchedPolicy != null ? String.valueOf(outcome.matchedPolicy.zoneId) : "null";
        String matchedZoneName = outcome.matchedPolicy != null ? outcome.matchedPolicy.zoneName : "null";
        String auditPayload = "verificationPayload={"
                + "verificationId=" + outcome.verificationId
                + ", "
                + "matchedPolicyId=" + matchedPolicyId
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
        if (verificationPolicies == null || verificationPolicies.isEmpty()) {
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

    private void fetchVerificationPoliciesFromServer() {
        // UI 스레드를 막지 않기 위해 네트워크 호출은 별도 스레드에서 수행한다.
        new Thread(() -> {
            HttpURLConnection connection = null;
            try {
                URL url = new URL(BACKEND_BASE_URL + "/api/v1/location-verification/policies/" + DEFAULT_UNIVERSITY_ID);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(7000);
                connection.setReadTimeout(7000);

                int responseCode = connection.getResponseCode();
                InputStream stream = responseCode >= 200 && responseCode < 300
                        ? connection.getInputStream()
                        : connection.getErrorStream();

                String body = readStream(stream);
                if (responseCode < 200 || responseCode >= 300) {
                    throw new IllegalStateException("정책 조회 실패 (HTTP " + responseCode + ")");
                }

                // 응답 JSON을 앱 내부 인증 모델로 변환한다.
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
                    Toast.makeText(
                            MainActivity.this,
                            "인증 구역을 불러오지 못했습니다.",
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

    @NonNull
    private List<VerificationPolicy> parsePolicies(@NonNull String rawJson) throws Exception {
        // 백엔드 응답은 정책 배열이며, 중심 좌표와 반경 정보를 포함한다.
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
        if (stream == null) {
            return "";
        }

        // 응답 바디를 문자열로 모아 JSON 파서에 전달한다.
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            StringBuilder builder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }
            return builder.toString();
        }
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

        // 인증 기준 한 건을 표현하는 단순 모델이다.
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

        // UI 표기와 DB 저장에 공통으로 쓸 수 있도록 판정 결과를 묶는다.
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

        private final long verificationId;
        private final double savedLatitude;
        private final double savedLongitude;

        private boolean hasSavedLocation() {
            return !Double.isNaN(savedLatitude) && !Double.isNaN(savedLongitude);
        }
    }
}
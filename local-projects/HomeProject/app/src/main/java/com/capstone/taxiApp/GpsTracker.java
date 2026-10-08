package com.capstone.taxiApp;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import androidx.core.app.ActivityCompat;
import androidx.annotation.NonNull;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;

public class GpsTracker {
    private static final long MAX_LAST_LOCATION_AGE_MILLIS = 5 * 60 * 1000L;
    private final Context mContext;
    private final FusedLocationProviderClient fusedLocationClient;

    // 위치 서비스 클라이언트를 초기화해 현재 좌표를 조회할 준비를 한다.
    public GpsTracker(Context context) {
        this.mContext = context;
        this.fusedLocationClient = LocationServices.getFusedLocationProviderClient(mContext);
    }

    public void getCurrentLocation(OnLocationReceivedListener listener) {
        // 위치 권한(FINE/COARSE) 중 하나라도 허용되어야 조회 가능
        boolean fineGranted = ActivityCompat.checkSelfPermission(
                mContext,
                Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED;
        boolean coarseGranted = ActivityCompat.checkSelfPermission(
                mContext,
                Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED;

        if (!fineGranted && !coarseGranted) {
            listener.onFailed("위치 권한이 없어 인증할 수 없습니다.");
            return;
        }

        // 우선 실시간 위치를 요청하고, 실패하거나 null이면 마지막 위치로 한 번 더 보완한다.
        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        listener.onReceived(LocationSnapshot.fromLocation(location));
                    } else {
                        fallbackToLastKnownLocation(listener);
                    }
                })
                .addOnFailureListener(e -> fallbackToLastKnownLocation(listener));
    }

    private void fallbackToLastKnownLocation(OnLocationReceivedListener listener) {
        // 센서 응답이 늦거나 실시간 위치가 비어 있을 때 마지막 캐시 위치를 대체값으로 사용한다.
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(location -> {
                    if (location != null && isFreshEnough(location)) {
                        listener.onReceived(LocationSnapshot.fromLocation(location));
                    } else {
                        listener.onFailed("실시간 GPS 좌표를 가져오지 못했습니다. 에뮬레이터라면 Extended controls > Location에서 현재 위치를 먼저 설정하세요.");
                    }
                })
                .addOnFailureListener(e -> listener.onFailed("위치 조회 실패: " + e.getMessage()));
    }

    private boolean isFreshEnough(@NonNull Location location) {
        long capturedAt = location.getTime();
        if (capturedAt <= 0L) {
            return false;
        }
        long ageMillis = System.currentTimeMillis() - capturedAt;
        return ageMillis >= 0L && ageMillis <= MAX_LAST_LOCATION_AGE_MILLIS;
    }

    // 위치 조회 결과를 호출부(MainActivity)로 전달하는 인터페이스
    public interface OnLocationReceivedListener {
        void onReceived(@NonNull LocationSnapshot snapshot);
        void onFailed(@NonNull String message);
    }

    public static final class LocationSnapshot {
        private final double latitude;
        private final double longitude;
        private final float accuracyMeters;
        private final long capturedAtEpochMillis;

        // 인증 로직에서 필요한 최소 위치 정보만 별도 객체로 정리한다.
        private LocationSnapshot(double latitude, double longitude, float accuracyMeters, long capturedAtEpochMillis) {
            this.latitude = latitude;
            this.longitude = longitude;
            this.accuracyMeters = accuracyMeters;
            this.capturedAtEpochMillis = capturedAtEpochMillis;
        }

        @NonNull
        public static LocationSnapshot fromLocation(@NonNull Location location) {
            // Android Location 객체를 앱 내부에서 다루기 쉬운 형태로 변환한다.
            return new LocationSnapshot(
                    location.getLatitude(),
                    location.getLongitude(),
                    location.hasAccuracy() ? location.getAccuracy() : 0f,
                    location.getTime()
            );
        }

        public double getLatitude() {
            return latitude;
        }

        public double getLongitude() {
            return longitude;
        }

        public float getAccuracyMeters() {
            return accuracyMeters;
        }

        public long getCapturedAtEpochMillis() {
            return capturedAtEpochMillis;
        }
    }
}

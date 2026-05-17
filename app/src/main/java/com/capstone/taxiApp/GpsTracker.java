package com.capstone.taxiApp;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;

public class GpsTracker {

    private static final long MAX_LAST_LOCATION_AGE_MILLIS = 5 * 60 * 1000L;

    private final Context context;
    private final FusedLocationProviderClient fusedLocationClient;

    public GpsTracker(Context context) {
        this.context = context;
        this.fusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
    }

    public void getCurrentLocation(@NonNull OnLocationReceivedListener listener) {
        boolean fineGranted = ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED;
        boolean coarseGranted = ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED;

        if (!fineGranted && !coarseGranted) {
            listener.onFailed("위치 권한이 없어 인증할 수 없습니다.");
            return;
        }

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

    private void fallbackToLastKnownLocation(@NonNull OnLocationReceivedListener listener) {
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

    public interface OnLocationReceivedListener {
        void onReceived(@NonNull LocationSnapshot snapshot);
        void onFailed(@NonNull String message);
    }

    public static final class LocationSnapshot {
        private final double latitude;
        private final double longitude;
        private final float accuracyMeters;
        private final long capturedAtEpochMillis;

        private LocationSnapshot(double latitude, double longitude, float accuracyMeters, long capturedAtEpochMillis) {
            this.latitude = latitude;
            this.longitude = longitude;
            this.accuracyMeters = accuracyMeters;
            this.capturedAtEpochMillis = capturedAtEpochMillis;
        }

        @NonNull
        public static LocationSnapshot fromLocation(@NonNull Location location) {
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

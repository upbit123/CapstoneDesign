package com.capstone.taxiApp;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;

// 앱에서 백엔드 사용자 세션(userId, universityId, email)을 간단히 저장하는 로컬 세션 헬퍼.
public class SessionManager {

    private static final String PREFS_NAME = "taxi_app_session";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_UNIVERSITY_ID = "university_id";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_NAME = "name";
    private static final String KEY_NICKNAME = "nickname";
    private static final String KEY_PHONE = "phone";
    private static final String KEY_PROFILE_COMPLETED = "profile_completed";

    private final SharedPreferences preferences;

    public SessionManager(@NonNull Context context) {
        preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public void saveSession(long userId, long universityId, @NonNull String email, @NonNull String name) {
        saveSession(userId, universityId, email, name, "", "", false);
    }

    public void saveSession(
            long userId,
            long universityId,
            @NonNull String email,
            @NonNull String name,
            @NonNull String nickname,
            @NonNull String phone,
            boolean profileCompleted
    ) {
        preferences.edit()
                .putLong(KEY_USER_ID, userId)
                .putLong(KEY_UNIVERSITY_ID, universityId)
                .putString(KEY_EMAIL, email)
                .putString(KEY_NAME, name)
                .putString(KEY_NICKNAME, nickname)
                .putString(KEY_PHONE, phone)
                .putBoolean(KEY_PROFILE_COMPLETED, profileCompleted)
                .apply();
    }

    public long getUserId() {
        return preferences.getLong(KEY_USER_ID, -1L);
    }

    public long getUniversityId() {
        return preferences.getLong(KEY_UNIVERSITY_ID, -1L);
    }

    public boolean hasActiveSession() {
        return getUserId() > 0L && getUniversityId() > 0L;
    }

    public boolean isProfileCompleted() {
        return preferences.getBoolean(KEY_PROFILE_COMPLETED, false);
    }

    @NonNull
    public String getDisplayName() {
        String nickname = preferences.getString(KEY_NICKNAME, "");
        if (nickname != null && !nickname.trim().isEmpty()) {
            return nickname.trim();
        }

        String name = preferences.getString(KEY_NAME, "");
        return name == null ? "" : name.trim();
    }

    public void clearSession() {
        preferences.edit().clear().apply();
    }
}

package com.capstone.taxiApp;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

// Firebase 현재 사용자의 최신 ID 토큰을 비동기로 가져오는 헬퍼.
public final class FirebaseTokenHelper {

    private FirebaseTokenHelper() {
    }

    public interface TokenCallback {
        void onSuccess(@NonNull String idToken);
        void onFailure(@NonNull String message);
    }

    public static void fetchIdToken(@NonNull TokenCallback callback) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            callback.onFailure("Firebase 로그인 사용자가 없습니다.");
            return;
        }

        currentUser.getIdToken(true).addOnCompleteListener(task -> {
            if (!task.isSuccessful() || task.getResult() == null || task.getResult().getToken() == null) {
                String message = task.getException() != null
                        ? task.getException().getMessage()
                        : "Firebase 토큰 발급에 실패했습니다.";
                callback.onFailure(message);
                return;
            }

            callback.onSuccess(task.getResult().getToken());
        });
    }
}

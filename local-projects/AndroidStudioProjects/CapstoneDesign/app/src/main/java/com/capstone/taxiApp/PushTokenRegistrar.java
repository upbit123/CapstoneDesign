package com.capstone.taxiApp;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.messaging.FirebaseMessaging;

public final class PushTokenRegistrar {

    private static final String TAG = "PushTokenRegistrar";

    private PushTokenRegistrar() {
    }

    public static void syncIfPossible(@NonNull Context context) {
        SessionManager sessionManager = new SessionManager(context);
        if (!sessionManager.hasActiveSession()) {
            return;
        }

        FirebaseTokenHelper.fetchIdToken(new FirebaseTokenHelper.TokenCallback() {
            @Override
            public void onSuccess(@NonNull String idToken) {
                FirebaseMessaging.getInstance().getToken()
                        .addOnCompleteListener(task -> {
                            if (!task.isSuccessful() || task.getResult() == null || task.getResult().isBlank()) {
                                if (task.getException() != null) {
                                    Log.w(TAG, "Failed to fetch FCM token", task.getException());
                                }
                                return;
                            }

                            String pushToken = task.getResult();
                            new Thread(() -> {
                                try {
                                    new PushTokenRepository().updatePushToken(idToken, pushToken);
                                } catch (Exception exception) {
                                    Log.w(TAG, "Failed to sync push token", exception);
                                }
                            }).start();
                        });
            }

            @Override
            public void onFailure(@NonNull String message) {
                Log.w(TAG, "Skipping push token sync: " + message);
            }
        });
    }
}

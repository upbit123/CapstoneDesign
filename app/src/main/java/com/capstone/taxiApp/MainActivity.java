package com.capstone.taxiApp;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_POST_NOTIFICATIONS = 1001;
    private FirebaseAuth auth;
    private SessionManager sessionManager;
    private BackendAuthRepository backendAuthRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        auth = FirebaseAuth.getInstance();
        sessionManager = new SessionManager(this);
        backendAuthRepository = new BackendAuthRepository();
        requestNotificationPermissionIfNeeded();

        if (auth.getCurrentUser() != null && sessionManager.hasActiveSession() && sessionManager.isProfileCompleted()) {
            PushTokenRegistrar.syncIfPossible(this);
            startActivity(new Intent(this, HomeActivity.class));
            finish();
            return;
        }

        TextInputEditText studentIdEditText = findViewById(R.id.studentIdEditText);
        TextInputEditText passwordEditText = findViewById(R.id.passwordEditText);
        Button loginButton = findViewById(R.id.loginButton);
        Button registerButton = findViewById(R.id.registerButton);
        Button forgotPasswordButton = findViewById(R.id.forgotPasswordButton);
        Button locationCheckButton = findViewById(R.id.locationCheckButton);

        loginButton.setOnClickListener(v -> {
            String studentId = getText(studentIdEditText);
            String password = getText(passwordEditText);

            if (studentId.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "학번과 비밀번호를 입력하세요.", Toast.LENGTH_SHORT).show();
                return;
            }

            new Thread(() -> {
                try {
                    String email = backendAuthRepository.lookupLoginEmailByStudentId(studentId);
                    runOnUiThread(() -> auth.signInWithEmailAndPassword(email, password)
                            .addOnCompleteListener(this, task -> {
                                if (task.isSuccessful()) {
                                    syncFirebaseUserToBackend(studentId);
                                } else {
                                    String errorMsg = task.getException() != null
                                            ? task.getException().getMessage()
                                            : "정보를 확인하세요.";
                                    Toast.makeText(this, "로그인 실패: " + errorMsg, Toast.LENGTH_LONG).show();
                                }
                            }));
                } catch (Exception exception) {
                    runOnUiThread(() -> Toast.makeText(
                            this,
                            "로그인용 이메일 조회 실패: " + exception.getMessage(),
                            Toast.LENGTH_LONG
                    ).show());
                }
            }).start();
        });

        registerButton.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, RegisterActivity.class)));

        forgotPasswordButton.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, PasswordResetActivity.class)));

        locationCheckButton.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, LocationCheckActivity.class)));
    }

    private void syncFirebaseUserToBackend(@NonNull String studentId) {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Firebase 사용자 정보를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        currentUser.getIdToken(true).addOnCompleteListener(task -> {
            if (!task.isSuccessful() || task.getResult() == null || task.getResult().getToken() == null) {
                Toast.makeText(this, "로그인 토큰 발급에 실패했습니다.", Toast.LENGTH_LONG).show();
                return;
            }

            String idToken = task.getResult().getToken();
            String displayName = currentUser.getDisplayName() == null
                    ? ""
                    : currentUser.getDisplayName();

            new Thread(() -> {
                try {
                    BackendAuthRepository.BackendLoginResult result =
                            backendAuthRepository.loginWithFirebase(idToken, studentId, displayName);
                    sessionManager.saveSession(
                            result.userId(),
                            result.universityId(),
                            result.email(),
                            result.name(),
                            result.nickname(),
                            result.phone(),
                            result.profileCompleted()
                    );
                    runOnUiThread(() -> {
                        PushTokenRegistrar.syncIfPossible(MainActivity.this);
                        Toast.makeText(
                                MainActivity.this,
                                result.message(),
                                Toast.LENGTH_SHORT
                        ).show();

                        if (!result.profileCompleted()) {
                            startActivity(new Intent(MainActivity.this, ProfileSetupActivity.class));
                            finish();
                        } else {
                            startActivity(new Intent(MainActivity.this, HomeActivity.class));
                            finish();
                        }
                    });
                } catch (Exception exception) {
                    auth.signOut();
                    runOnUiThread(() -> Toast.makeText(
                            MainActivity.this,
                            "백엔드 계정 연동 실패: " + exception.getMessage(),
                            Toast.LENGTH_LONG
                    ).show());
                }
            }).start();
        });
    }

    private String getText(TextInputEditText editText) {
        return editText.getText() == null ? "" : editText.getText().toString().trim();
    }

    private void requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            return;
        }

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS)
                == PackageManager.PERMISSION_GRANTED) {
            return;
        }

        ActivityCompat.requestPermissions(
                this,
                new String[]{android.Manifest.permission.POST_NOTIFICATIONS},
                REQUEST_POST_NOTIFICATIONS
        );
    }
}

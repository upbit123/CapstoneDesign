package com.capstone.taxiApp;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;

public class RegisterActivity extends AppCompatActivity {

    private static final String SCHOOL_EMAIL_DOMAIN = "syuin.ac.kr";
    private static final String TAG = "RegisterActivity";

    private FirebaseAuth auth;
    private SignupVerificationRepository signupVerificationRepository;
    private TextInputEditText studentIdEditText;
    private TextInputEditText emailEditText;
    private TextInputEditText verifyCodeEditText;
    private TextInputEditText passwordEditText;
    private TextInputEditText confirmPasswordEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        auth = FirebaseAuth.getInstance();
        signupVerificationRepository = new SignupVerificationRepository();

        studentIdEditText = findViewById(R.id.studentIdEditText);
        emailEditText = findViewById(R.id.emailEditText);
        verifyCodeEditText = findViewById(R.id.verifyCodeEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        confirmPasswordEditText = findViewById(R.id.confirmPasswordEditText);

        Button backButton = findViewById(R.id.backButton);
        Button sendCodeButton = findViewById(R.id.sendCodeButton);
        Button registerButton = findViewById(R.id.doRegisterButton);

        backButton.setOnClickListener(v -> finish());
        sendCodeButton.setOnClickListener(v -> sendVerificationCode());
        registerButton.setOnClickListener(v -> registerUser());
    }

    private void sendVerificationCode() {
        String studentId = getText(studentIdEditText);
        String email = getText(emailEditText);
        if (studentId.isEmpty()) {
            Toast.makeText(this, "학번을 먼저 입력해 주세요.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (email.isEmpty() || !email.endsWith("@" + SCHOOL_EMAIL_DOMAIN)) {
            Toast.makeText(this, "@" + SCHOOL_EMAIL_DOMAIN + " 이메일을 입력해 주세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(this, "인증코드를 전송 중입니다...", Toast.LENGTH_SHORT).show();

        new Thread(() -> {
            try {
                signupVerificationRepository.sendVerificationCode(studentId, email);
                runOnUiThread(() -> Toast.makeText(
                        RegisterActivity.this,
                        "인증코드가 발송되었습니다.",
                        Toast.LENGTH_SHORT
                ).show());
            } catch (Exception exception) {
                runOnUiThread(() -> Toast.makeText(
                        RegisterActivity.this,
                        "메일 발송 실패: " + exception.getMessage(),
                        Toast.LENGTH_LONG
                ).show());
            }
        }).start();
    }

    private void registerUser() {
        String studentId = getText(studentIdEditText);
        String email = getText(emailEditText);
        String inputCode = getText(verifyCodeEditText);
        String password = getText(passwordEditText);
        String confirmPassword = getText(confirmPasswordEditText);

        if (studentId.isEmpty() || email.isEmpty() || inputCode.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, "모든 정보를 입력해 주세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!email.endsWith("@" + SCHOOL_EMAIL_DOMAIN)) {
            Toast.makeText(this, "학교 이메일만 사용할 수 있습니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "비밀번호가 일치하지 않습니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.length() < 6) {
            Toast.makeText(this, "비밀번호는 6자리 이상이어야 합니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        new Thread(() -> {
            try {
                signupVerificationRepository.verifyCode(studentId, email, inputCode);
                runOnUiThread(() -> auth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(this, task -> {
                            if (task.isSuccessful()) {
                                syncNewUserToBackend(studentId);
                            } else {
                                Exception exception = task.getException();
                                if (exception instanceof FirebaseAuthUserCollisionException) {
                                    signInExistingAccountAndSync(studentId, email, password);
                                    return;
                                }
                                if (exception != null) {
                                    Log.e(TAG, "Firebase signup failed", exception);
                                }
                                Toast.makeText(
                                        this,
                                        buildFirebaseSignupErrorMessage(exception),
                                        Toast.LENGTH_LONG
                                ).show();
                            }
                        }));
            } catch (Exception exception) {
                runOnUiThread(() -> Toast.makeText(
                        RegisterActivity.this,
                        "학생 인증 실패: " + exception.getMessage(),
                        Toast.LENGTH_LONG
                ).show());
            }
        }).start();
    }

    private void signInExistingAccountAndSync(
            @NonNull String studentId,
            @NonNull String email,
            @NonNull String password
    ) {
        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, signInTask -> {
                    if (signInTask.isSuccessful()) {
                        Toast.makeText(
                                this,
                                "이미 가입된 이메일입니다. 기존 계정을 연결합니다.",
                                Toast.LENGTH_LONG
                        ).show();
                        syncNewUserToBackend(studentId);
                        return;
                    }

                    Exception signInException = signInTask.getException();
                    if (signInException != null) {
                        Log.e(TAG, "Existing Firebase account sign-in failed", signInException);
                    }
                    String message = signInException == null
                            ? "이미 가입된 이메일입니다. 비밀번호를 확인하거나 비밀번호 찾기를 이용하세요."
                            : "이미 가입된 이메일입니다. 비밀번호를 확인하거나 비밀번호 찾기를 이용하세요. "
                            + signInException.getMessage();
                    Toast.makeText(this, message, Toast.LENGTH_LONG).show();
                });
    }

    private void syncNewUserToBackend(@NonNull String studentId) {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "회원가입 후 사용자 정보를 찾을 수 없습니다.", Toast.LENGTH_LONG).show();
            return;
        }

        currentUser.getIdToken(true).addOnCompleteListener(task -> {
            if (!task.isSuccessful() || task.getResult() == null || task.getResult().getToken() == null) {
                Toast.makeText(this, "회원가입 토큰 발급에 실패했습니다.", Toast.LENGTH_LONG).show();
                return;
            }

            String idToken = task.getResult().getToken();
            String displayName = currentUser.getDisplayName() == null ? "" : currentUser.getDisplayName();

            new Thread(() -> {
                try {
                    backendSyncAfterSignup(idToken, studentId, displayName);
                    runOnUiThread(() -> {
                        Toast.makeText(this, "회원가입이 완료되었습니다.", Toast.LENGTH_SHORT).show();
                        auth.signOut();
                        finish();
                    });
                } catch (Exception exception) {
                    runOnUiThread(() -> Toast.makeText(
                            this,
                            "백엔드 계정 연동 실패: " + exception.getMessage(),
                            Toast.LENGTH_LONG
                    ).show());
                }
            }).start();
        });
    }

    private void backendSyncAfterSignup(
            @NonNull String idToken,
            @NonNull String studentId,
            @NonNull String displayName
    ) throws Exception {
        BackendAuthRepository backendAuthRepository = new BackendAuthRepository();
        backendAuthRepository.loginWithFirebase(idToken, studentId, displayName);
    }

    private String getText(TextInputEditText editText) {
        return editText.getText() == null ? "" : editText.getText().toString().trim();
    }

    private String buildFirebaseSignupErrorMessage(Exception exception) {
        if (exception == null) {
            return "가입 오류: 알 수 없는 오류";
        }

        if (exception instanceof FirebaseNetworkException) {
            return "가입 오류: Firebase 네트워크 오류입니다. 에뮬레이터/기기의 인터넷 연결과 시간을 확인하세요.";
        }

        if (exception instanceof FirebaseAuthException) {
            FirebaseAuthException firebaseAuthException = (FirebaseAuthException) exception;
            return "가입 오류: " + firebaseAuthException.getErrorCode() + " - " + firebaseAuthException.getMessage();
        }

        return "가입 오류: " + exception.getClass().getSimpleName() + " - " + exception.getMessage();
    }
}

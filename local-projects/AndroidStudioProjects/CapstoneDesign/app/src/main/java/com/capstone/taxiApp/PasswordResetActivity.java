package com.capstone.taxiApp;

import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;

public class PasswordResetActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private BackendAuthRepository backendAuthRepository;
    private TextInputEditText studentIdEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password_reset);

        auth = FirebaseAuth.getInstance();
        backendAuthRepository = new BackendAuthRepository();

        studentIdEditText = findViewById(R.id.studentIdEditText);
        Button backButton = findViewById(R.id.backButton);
        Button sendResetButton = findViewById(R.id.sendResetButton);

        backButton.setOnClickListener(v -> finish());
        sendResetButton.setOnClickListener(v -> sendResetEmail());
    }

    private void sendResetEmail() {
        String studentId = getText(studentIdEditText);
        if (studentId.isEmpty()) {
            Toast.makeText(this, "학번을 입력하세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(this, "재설정 메일을 준비 중입니다...", Toast.LENGTH_SHORT).show();

        new Thread(() -> {
            try {
                String email = backendAuthRepository.lookupLoginEmailByStudentId(studentId);
                runOnUiThread(() -> auth.sendPasswordResetEmail(email)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Toast.makeText(
                                        PasswordResetActivity.this,
                                        "비밀번호 재설정 메일을 발송했습니다.",
                                        Toast.LENGTH_LONG
                                ).show();
                                finish();
                            } else {
                                String error = task.getException() != null
                                        ? task.getException().getMessage()
                                        : "재설정 메일 발송에 실패했습니다.";
                                Toast.makeText(
                                        PasswordResetActivity.this,
                                        "비밀번호 재설정 실패: " + error,
                                        Toast.LENGTH_LONG
                                ).show();
                            }
                        }));
            } catch (Exception exception) {
                runOnUiThread(() -> Toast.makeText(
                        PasswordResetActivity.this,
                        "학번 조회 실패: " + exception.getMessage(),
                        Toast.LENGTH_LONG
                ).show());
            }
        }).start();
    }

    private String getText(TextInputEditText editText) {
        return editText.getText() == null ? "" : editText.getText().toString().trim();
    }
}

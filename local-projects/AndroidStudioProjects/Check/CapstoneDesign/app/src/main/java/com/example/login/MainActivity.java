package com.example.login;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final String ADMIN_CODE = "100772";
    
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    
    private TextInputEditText studentIdEditText, passwordEditText, adminCodeEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference("login_logs");

        studentIdEditText = findViewById(R.id.studentIdEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        adminCodeEditText = findViewById(R.id.adminCodeEditText);
        Button loginButton = findViewById(R.id.loginButton);
        Button registerButton = findViewById(R.id.registerButton);
        ImageButton backButton = findViewById(R.id.backButton);

        // Open Register Activity
        registerButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
            startActivity(intent);
        });

        // 1. Simple Login Logic (Student ID + Password)
        loginButton.setOnClickListener(v -> {
            String studentId = studentIdEditText.getText() != null ? studentIdEditText.getText().toString().trim() : "";
            String password = passwordEditText.getText() != null ? passwordEditText.getText().toString().trim() : "";

            if (studentId.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "학번과 비밀번호를 입력하세요.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Convert student ID to internal email format used during registration
            String email = studentId + "@syuin.ac.kr";

            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            proceedToMainScreen(email);
                        } else {
                            Toast.makeText(MainActivity.this, "로그인 실패. 학번 또는 비밀번호를 확인하세요.", Toast.LENGTH_SHORT).show();
                        }
                    });
        });
        
        backButton.setOnClickListener(v -> onBackPressed());

        // Admin Mode listener
        adminCodeEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().equals(ADMIN_CODE)) {
                    enterAdminMode();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void enterAdminMode() {
        Toast.makeText(this, "관리자 모드 실행", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(this, AdminLogActivity.class);
        startActivity(intent);
        adminCodeEditText.setText("");
    }

    private void proceedToMainScreen(String email) {
        // Log the login event
        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
        AdminLogActivity.LogEntry logEntry = new AdminLogActivity.LogEntry(email, timestamp);
        mDatabase.push().setValue(logEntry);

        Toast.makeText(this, "로그인 성공!", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(this, HomeActivity.class);
        startActivity(intent);
        finish();
    }
}
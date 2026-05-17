package com.capstone.taxiApp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        auth = FirebaseAuth.getInstance();

        TextInputEditText studentIdEditText = findViewById(R.id.studentIdEditText);
        TextInputEditText passwordEditText = findViewById(R.id.passwordEditText);
        Button loginButton = findViewById(R.id.loginButton);
        Button registerButton = findViewById(R.id.registerButton);
        Button locationCheckButton = findViewById(R.id.locationCheckButton);

        loginButton.setOnClickListener(v -> {
            String studentId = getText(studentIdEditText);
            String password = getText(passwordEditText);

            if (studentId.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "학번과 비밀번호를 입력하세요.", Toast.LENGTH_SHORT).show();
                return;
            }

            String email = studentId + "@syuin.ac.kr";
            auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(this, "로그인 성공", Toast.LENGTH_SHORT).show();
                        } else {
                            String errorMsg = task.getException() != null
                                    ? task.getException().getMessage()
                                    : "정보를 확인하세요.";
                            Toast.makeText(this, "로그인 실패: " + errorMsg, Toast.LENGTH_LONG).show();
                        }
                    });
        });

        registerButton.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, RegisterActivity.class)));

        locationCheckButton.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, LocationCheckActivity.class)));
    }

    private String getText(TextInputEditText editText) {
        return editText.getText() == null ? "" : editText.getText().toString().trim();
    }
}

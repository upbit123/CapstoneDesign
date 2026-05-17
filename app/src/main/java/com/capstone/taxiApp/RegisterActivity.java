package com.capstone.taxiApp;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Locale;
import java.util.Properties;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class RegisterActivity extends AppCompatActivity {

    private static final String TAG = "RegisterActivity";
    private static final String SENDER_EMAIL = "kimjiihan986@gmail.com";
    private static final String SENDER_PASSWORD = "mdcs itds hiic yver";

    private final ExecutorService mailExecutor = Executors.newSingleThreadExecutor();

    private FirebaseAuth auth;
    private TextInputEditText studentIdEditText;
    private TextInputEditText emailEditText;
    private TextInputEditText verifyCodeEditText;
    private TextInputEditText passwordEditText;
    private TextInputEditText confirmPasswordEditText;
    private String generatedCode = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        auth = FirebaseAuth.getInstance();

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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mailExecutor.shutdownNow();
    }

    private void sendVerificationCode() {
        String email = getText(emailEditText);
        if (email.isEmpty() || !email.endsWith("@syuin.ac.kr")) {
            Toast.makeText(this, "@syuin.ac.kr 이메일을 입력해 주세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        generatedCode = String.format(Locale.getDefault(), "%06d", new Random().nextInt(1_000_000));
        Toast.makeText(this, "인증코드를 전송 중입니다...", Toast.LENGTH_SHORT).show();

        mailExecutor.execute(() -> {
            boolean success = sendVerificationEmail(email, generatedCode);
            runOnUiThread(() -> Toast.makeText(
                    RegisterActivity.this,
                    success ? "인증코드가 발송되었습니다." : "메일 발송 실패",
                    Toast.LENGTH_SHORT
            ).show());
        });
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

        if (!email.endsWith("@syuin.ac.kr")) {
            Toast.makeText(this, "학교 이메일만 사용할 수 있습니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (generatedCode.isEmpty() || !generatedCode.equals(inputCode)) {
            Toast.makeText(this, "인증코드가 일치하지 않습니다.", Toast.LENGTH_SHORT).show();
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

        String internalEmail = studentId + "@syuin.ac.kr";
        auth.createUserWithEmailAndPassword(internalEmail, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "회원가입이 완료되었습니다.", Toast.LENGTH_SHORT).show();
                        auth.signOut();
                        finish();
                    } else {
                        String error = task.getException() != null ? task.getException().getMessage() : "가입 실패";
                        Toast.makeText(this, "가입 오류: " + error, Toast.LENGTH_LONG).show();
                    }
                });
    }

    private boolean sendVerificationEmail(String recipientEmail, String code) {
        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.socketFactory.port", "465");
        props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.port", "465");

        Session session = Session.getDefaultInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(SENDER_EMAIL, SENDER_PASSWORD);
            }
        });

        try {
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(SENDER_EMAIL));
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(recipientEmail));
            message.setSubject("[SU TAXI] 본인인증 코드입니다.");
            message.setText("인증코드는 [" + code + "] 입니다.");
            Transport.send(message);
            return true;
        } catch (MessagingException e) {
            Log.e(TAG, "Failed to send verification email", e);
            return false;
        }
    }

    private String getText(TextInputEditText editText) {
        return editText.getText() == null ? "" : editText.getText().toString().trim();
    }
}

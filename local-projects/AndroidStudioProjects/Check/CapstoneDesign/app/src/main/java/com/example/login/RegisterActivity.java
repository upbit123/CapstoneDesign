package com.example.login;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Locale;
import java.util.Properties;
import java.util.Random;

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

    private FirebaseAuth mAuth;
    private TextInputEditText studentIdEditText, emailEditText, verifyCodeEditText, passwordEditText, confirmPasswordEditText;
    private String generatedCode = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();

        studentIdEditText = findViewById(R.id.studentIdEditText);
        emailEditText = findViewById(R.id.emailEditText);
        verifyCodeEditText = findViewById(R.id.verifyCodeEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        confirmPasswordEditText = findViewById(R.id.confirmPasswordEditText);
        
        Button sendCodeButton = findViewById(R.id.sendCodeButton);
        Button doRegisterButton = findViewById(R.id.doRegisterButton);
        ImageButton backButton = findViewById(R.id.backButton);

        if (backButton != null) backButton.setOnClickListener(v -> finish());

        if (sendCodeButton != null) {
            sendCodeButton.setOnClickListener(v -> {
                String email = emailEditText.getText().toString().trim();
                if (email.isEmpty() || !email.endsWith("@syuin.ac.kr")) {
                    Toast.makeText(this, "@syuin.ac.kr 이메일을 입력해 주세요.", Toast.LENGTH_SHORT).show();
                    return;
                }
                generatedCode = String.format(Locale.getDefault(), "%06d", new Random().nextInt(1000000));
                new SendEmailTask(email, generatedCode).execute();
                Toast.makeText(this, "인증코드를 전송 중입니다...", Toast.LENGTH_SHORT).show();
            });
        }

        if (doRegisterButton != null) {
            doRegisterButton.setOnClickListener(v -> registerUser());
        }
    }

    private void registerUser() {
        String studentId = studentIdEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();
        String inputCode = verifyCodeEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        String confirmPassword = confirmPasswordEditText.getText().toString().trim();

        if (studentId.isEmpty() || email.isEmpty() || inputCode.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "모든 정보를 입력해 주세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (generatedCode.isEmpty() || !inputCode.equals(generatedCode)) {
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

        mAuth.createUserWithEmailAndPassword(internalEmail, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // 성공 시 "인증성공" 토스트 띄우고 즉시 종료
                        Toast.makeText(getApplicationContext(), "인증성공", Toast.LENGTH_SHORT).show();
                        mAuth.signOut();
                        finish(); 
                    } else {
                        String error = task.getException() != null ? task.getException().getMessage() : "가입 실패";
                        Toast.makeText(RegisterActivity.this, "가입 오류: " + error, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private class SendEmailTask extends AsyncTask<Void, Void, Boolean> {
        private String recipientEmail;
        private String code;

        public SendEmailTask(String recipientEmail, String code) {
            this.recipientEmail = recipientEmail;
            this.code = code;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            Properties props = new Properties();
            props.put("mail.smtp.host", "smtp.gmail.com");
            props.put("mail.smtp.socketFactory.port", "465");
            props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.port", "465");

            Session session = Session.getDefaultInstance(props, new Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(SENDER_EMAIL, SENDER_PASSWORD);
                }
            });

            try {
                MimeMessage mm = new MimeMessage(session);
                mm.setFrom(new InternetAddress(SENDER_EMAIL));
                mm.addRecipient(Message.RecipientType.TO, new InternetAddress(recipientEmail));
                mm.setSubject("[SU TAXI] 본인인증 코드입니다.");
                mm.setText("인증코드는 [" + code + "] 입니다.");
                Transport.send(mm);
                return true;
            } catch (MessagingException e) {
                Log.e("EMAIL_ERROR", "Error sending email", e);
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (success) {
                Toast.makeText(RegisterActivity.this, "인증코드가 발송되었습니다.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(RegisterActivity.this, "메일 발송 실패", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
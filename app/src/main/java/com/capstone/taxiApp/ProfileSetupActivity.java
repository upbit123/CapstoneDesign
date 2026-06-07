package com.capstone.taxiApp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;

public class ProfileSetupActivity extends AppCompatActivity {

    public static final String EXTRA_PROFILE_REQUIRED = "profile_required";

    private SessionManager sessionManager;
    private UserProfileRepository userProfileRepository;

    private TextInputEditText nameEditText;
    private TextInputEditText nicknameEditText;
    private TextInputEditText phoneEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_setup);

        sessionManager = new SessionManager(this);
        userProfileRepository = new UserProfileRepository();

        nameEditText = findViewById(R.id.nameEditText);
        nicknameEditText = findViewById(R.id.nicknameEditText);
        phoneEditText = findViewById(R.id.phoneEditText);
        Button saveButton = findViewById(R.id.saveProfileButton);

        saveButton.setOnClickListener(v -> saveProfile());
        loadProfile();
    }

    private void loadProfile() {
        FirebaseTokenHelper.fetchIdToken(new FirebaseTokenHelper.TokenCallback() {
            @Override
            public void onSuccess(@NonNull String idToken) {
                new Thread(() -> {
                    try {
                        UserProfileRepository.UserProfileResult profile = userProfileRepository.fetchMyProfile(idToken);
                        runOnUiThread(() -> {
                            nameEditText.setText(profile.name());
                            nicknameEditText.setText(profile.nickname());
                            phoneEditText.setText(profile.phone());
                        });
                    } catch (Exception exception) {
                        runOnUiThread(() -> Toast.makeText(
                                ProfileSetupActivity.this,
                                "프로필 조회 실패: " + exception.getMessage(),
                                Toast.LENGTH_LONG
                        ).show());
                    }
                }).start();
            }

            @Override
            public void onFailure(@NonNull String message) {
                Toast.makeText(ProfileSetupActivity.this, message, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void saveProfile() {
        String name = getText(nameEditText);
        String nickname = getText(nicknameEditText);
        String phone = getText(phoneEditText);

        if (name.isEmpty() || nickname.isEmpty() || phone.isEmpty()) {
            Toast.makeText(this, "이름, 닉네임, 휴대폰 번호를 입력해 주세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseTokenHelper.fetchIdToken(new FirebaseTokenHelper.TokenCallback() {
            @Override
            public void onSuccess(@NonNull String idToken) {
                new Thread(() -> {
                    try {
                        UserProfileRepository.UserProfileResult profile =
                                userProfileRepository.updateMyProfile(idToken, name, nickname, phone);
                        sessionManager.saveSession(
                                profile.userId(),
                                profile.universityId(),
                                profile.email(),
                                profile.name(),
                                profile.nickname(),
                                profile.phone(),
                                profile.profileCompleted()
                        );
                        runOnUiThread(() -> {
                            Toast.makeText(ProfileSetupActivity.this, "프로필이 저장되었습니다.", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(ProfileSetupActivity.this, HomeActivity.class));
                            finish();
                        });
                    } catch (Exception exception) {
                        runOnUiThread(() -> Toast.makeText(
                                ProfileSetupActivity.this,
                                "프로필 저장 실패: " + exception.getMessage(),
                                Toast.LENGTH_LONG
                        ).show());
                    }
                }).start();
            }

            @Override
            public void onFailure(@NonNull String message) {
                Toast.makeText(ProfileSetupActivity.this, message, Toast.LENGTH_LONG).show();
            }
        });
    }

    private String getText(TextInputEditText editText) {
        return editText.getText() == null ? "" : editText.getText().toString().trim();
    }
}

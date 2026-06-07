package com.capstone.taxiApp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class HomeActivity extends AppCompatActivity {

    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        sessionManager = new SessionManager(this);

        TextView welcomeTextView = findViewById(R.id.welcomeTextView);
        Button chooseDestinationButton = findViewById(R.id.chooseDestinationButton);
        Button profileButton = findViewById(R.id.profileButton);
        Button logoutButton = findViewById(R.id.logoutButton);

        welcomeTextView.setText(sessionManager.getDisplayName() + "님, 오늘도 안전하게 이동하세요.");

        chooseDestinationButton.setOnClickListener(v ->
                startActivity(new Intent(HomeActivity.this, DestinationSelectionActivity.class)));

        profileButton.setOnClickListener(v ->
                startActivity(new Intent(HomeActivity.this, ProfileSetupActivity.class)));

        logoutButton.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            sessionManager.clearSession();
            Intent intent = new Intent(HomeActivity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });
    }
}

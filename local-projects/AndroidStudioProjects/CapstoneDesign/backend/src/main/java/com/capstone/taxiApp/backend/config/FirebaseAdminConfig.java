package com.capstone.taxiApp.backend.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

@Configuration
public class FirebaseAdminConfig {

    @Bean
    public FirebaseApp firebaseApp() throws IOException {
        if (!FirebaseApp.getApps().isEmpty()) {
            return FirebaseApp.getInstance();
        }

        String credentialsPath = System.getenv("FIREBASE_SERVICE_ACCOUNT_PATH");
        if (credentialsPath == null || credentialsPath.isBlank()) {
            throw new IllegalStateException("FIREBASE_SERVICE_ACCOUNT_PATH 환경변수가 필요합니다.");
        }

        try (InputStream inputStream = new FileInputStream(credentialsPath)) {
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(inputStream))
                    .build();
            return FirebaseApp.initializeApp(options);
        }
    }
}

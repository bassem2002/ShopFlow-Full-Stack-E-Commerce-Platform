package com.shopflow.shopflow.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Configuration
public class FirebaseConfig {

    private static final Logger logger = LoggerFactory.getLogger(FirebaseConfig.class);

    @Bean
    public FirebaseApp firebaseApp() {
        if (!FirebaseApp.getApps().isEmpty()) {
            return FirebaseApp.getInstance();
        }

        try {
            GoogleCredentials credentials = null;

            // 1. Try FIREBASE_CONFIG_JSON environment variable
            String firebaseConfigJson = System.getenv("FIREBASE_CONFIG_JSON");
            if (firebaseConfigJson != null && !firebaseConfigJson.isBlank()) {
                logger.info("Initializing Firebase Admin SDK using FIREBASE_CONFIG_JSON environment variable.");
                try (InputStream is = new ByteArrayInputStream(firebaseConfigJson.getBytes(StandardCharsets.UTF_8))) {
                    credentials = GoogleCredentials.fromStream(is);
                }
            } else {
                // 2. Fall back to Application Default Credentials (GOOGLE_APPLICATION_CREDENTIALS)
                try {
                    credentials = GoogleCredentials.getApplicationDefault();
                    logger.info("Initializing Firebase Admin SDK using Google Application Default Credentials.");
                } catch (Exception e) {
                    logger.warn("Google Application Default Credentials not available: {}", e.getMessage());
                }
            }

            if (credentials == null) {
                logger.warn("Firebase Admin SDK disabled: No credentials configured via GOOGLE_APPLICATION_CREDENTIALS or FIREBASE_CONFIG_JSON.");
                return null;
            }

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(credentials)
                    .build();

            return FirebaseApp.initializeApp(options);
        } catch (Exception e) {
            logger.error("FATAL: Firebase Admin SDK initialization error: {}", e.getMessage());
            return null;
        }
    }
}

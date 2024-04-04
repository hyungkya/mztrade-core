package com.mztrade.hki.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.FileInputStream;
import java.io.IOException;

@Configuration
public class FirebaseInitializer {

    @Bean
    public FirebaseApp initializeApp() throws IOException {
        ClassPathResource resource = new ClassPathResource("serviceAccountKey.json");

        FirebaseOptions options = new FirebaseOptions.Builder()
                .setCredentials(GoogleCredentials.fromStream(resource.getInputStream()))
                .build();

        return FirebaseApp.initializeApp(options);
    }


    @Bean
    public FirebaseAuth getFirebaseAuth() throws IOException {
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance(initializeApp());
        return firebaseAuth;
    }
}

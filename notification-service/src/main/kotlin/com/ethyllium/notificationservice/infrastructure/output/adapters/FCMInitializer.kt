package com.ethyllium.notificationservice.infrastructure.output.adapters

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import jakarta.annotation.PostConstruct
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.ClassPathResource
import org.springframework.stereotype.Service
import java.io.IOException


@Service
class FCMInitializer(
    @Value("\${app.firebase-configuration-file}") private val firebaseConfigPath: String
) {

    private val logger = LoggerFactory.getLogger(this::class.java)

    @PostConstruct
    fun initialize() {
        try {
            val options = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(ClassPathResource(firebaseConfigPath).getInputStream()))
                .build()
            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options)
                logger.info("Firebase application initialized")
            }
        } catch (e: IOException) {
            logger.error(e.message)
        }
    }
}
package com.ethyllium.notificationservice.infrastructure.outbound.fcm

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.ClassPathResource
import org.springframework.stereotype.Service

@Service
class FCMInitializer(
    @Value("\${app.firebase-configuration-file}") private val firebaseConfigPath: String
) {
    init {
        if (FirebaseApp.getApps().isEmpty()) {
            val serviceAccount = ClassPathResource(firebaseConfigPath).inputStream

            val options = FirebaseOptions.builder().setCredentials(GoogleCredentials.fromStream(serviceAccount)).build()

            FirebaseApp.initializeApp(options)
            println("Firebase Admin SDK initialized successfully")
        }
    }
}
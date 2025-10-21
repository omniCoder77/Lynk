package com.lynk.messageservice.infrastructure.outbound.notification.push

import com.google.firebase.messaging.*
import com.google.gson.GsonBuilder
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.Duration
import java.util.concurrent.ExecutionException

@Service
class FCMService {
    private val logger = LoggerFactory.getLogger(this::class.java)

    @Throws(InterruptedException::class, ExecutionException::class)
    fun sendMessageToToken(request: NotificationRequest) {
        val message: Message = getPreconfiguredMessageToToken(request)
        val gson = GsonBuilder().setPrettyPrinting().create()
        val jsonOutput = gson.toJson(message)
        val response = sendAndGetResponse(message)
        logger.info("Sent message to token. Device token: " + request.token + ", " + response + " msg " + jsonOutput)
    }

    @Throws(InterruptedException::class, ExecutionException::class)
    private fun sendAndGetResponse(message: Message?): String? {
        return FirebaseMessaging.getInstance().sendAsync(message).get()
    }


    private fun getAndroidConfig(topic: String?): AndroidConfig? {
        return AndroidConfig.builder().setTtl(Duration.ofMinutes(2).toMillis()).setCollapseKey(topic)
            .setPriority(AndroidConfig.Priority.HIGH).setNotification(
                AndroidNotification.builder().setTag(topic).build()
            ).build()
    }

    private fun getApnsConfig(topic: String?): ApnsConfig? {
        return ApnsConfig.builder().setAps(Aps.builder().setCategory(topic).setThreadId(topic).build()).build()
    }

    private fun getPreconfiguredMessageToToken(request: NotificationRequest): Message {
        return getPreconfiguredMessageBuilder(request).setToken(request.token).build()
    }

    private fun getPreconfiguredMessageBuilder(request: NotificationRequest): Message.Builder {
        val androidConfig = getAndroidConfig(request.topic)
        val apnsConfig = getApnsConfig(request.topic)
        val notification: Notification? = Notification.builder().setTitle(request.title).setBody(request.body).build()
        return Message.builder().setApnsConfig(apnsConfig).setAndroidConfig(androidConfig).setNotification(notification)
    }
}
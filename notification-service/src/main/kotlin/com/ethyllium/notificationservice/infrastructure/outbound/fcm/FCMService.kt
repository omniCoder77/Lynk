package com.ethyllium.notificationservice.infrastructure.outbound.fcm

import com.ethyllium.notificationservice.domain.model.MessageType
import com.ethyllium.notificationservice.infrastructure.outbound.fcm.dto.ConversationChatNotificationRequest
import com.ethyllium.notificationservice.infrastructure.outbound.fcm.dto.RoomChatNotificationRequest
import com.google.api.core.ApiFuture
import com.google.api.core.ApiFutureCallback
import com.google.api.core.ApiFutures
import com.google.firebase.messaging.*
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Service
class FCMChatService {

    fun sendChatNotification(request: ConversationChatNotificationRequest): Mono<String> {
        if (request.token.isBlank()) {
            return Mono.error(IllegalArgumentException("Token cannot be blank"))
        }
        val message = Message.builder().setToken(request.token).setWebpushConfig(
            getWebConfig(
                body = request.body,
                chatId = request.conversationId,
                senderId = request.senderId,
                messageId = request.messageId,
                messageType = request.messageType
            )
        ).putAllData(
            buildChatData(
                chatId = request.conversationId,
                senderId = request.senderId,
                messageId = request.messageId,
                messageType = request.messageType,
            )
        ).build()

        return sendMessageAsync(message)
    }

    private fun sendMessageAsync(message: Message): Mono<String> {
        val apiFuture: ApiFuture<String> = FirebaseMessaging.getInstance().sendAsync(message)

        return Mono.create { sink ->
            ApiFutures.addCallback(apiFuture, object : ApiFutureCallback<String> {
                override fun onFailure(t: Throwable) {
                    sink.error(t)
                }

                override fun onSuccess(result: String) {
                    sink.success(result)
                }
            }, Runnable::run)
        }
    }

    fun subscribeToTopic(token: String, topic: String): Mono<TopicManagementResponse> {
        val apiFuture: ApiFuture<TopicManagementResponse> =
            FirebaseMessaging.getInstance().subscribeToTopicAsync(listOf(token), topic)

        return Mono.create { sink ->
            ApiFutures.addCallback(apiFuture, object : ApiFutureCallback<TopicManagementResponse> {
                override fun onFailure(t: Throwable) {
                    sink.error(t)
                }

                override fun onSuccess(result: TopicManagementResponse) {
                    sink.success(result)
                }
            }, Runnable::run)
        }
    }

    private fun buildChatData(
        chatId: String, senderId: String, messageId: String, messageType: MessageType
    ): Map<String, String> {
        val data = mutableMapOf(
            "type" to "chat_message",
            "chatId" to chatId,
            "senderId" to senderId,
            "messageId" to messageId,
            "timestamp" to System.currentTimeMillis().toString(),
            "messageType" to messageType.name
        )

        return data
    }

    private fun getWebConfig(
        body: String,
        chatId: String,
        senderId: String,
        messageId: String,
        messageType: MessageType
    ): WebpushConfig {
        return WebpushConfig.builder().setNotification(
            WebpushNotification.builder().setBody(body).setTag("chat_${chatId}").setRenotify(true)
                .addAllActions(
                    mutableListOf(
                        WebpushNotification.Action(
                            "mark_read", "Mark as Read", "/icons/check.png"
                        )
                    )
                ).putAllCustomData(buildChatData(chatId, senderId, messageId, messageType)).build()
        ).setFcmOptions(
            WebpushFcmOptions.builder().setLink("/chat/${chatId}").build()
        ).putHeader("TTL", "86400").putHeader("Urgency", "high").putHeader("Topic", "chat_${chatId}").build()
    }

    fun sendRoomNotification(request: RoomChatNotificationRequest): Mono<String> {
        val message = Message.builder().setTopic(request.topic).setWebpushConfig(
            getWebConfig(
                body = request.body,
                chatId = request.topic,
                senderId = request.senderPhoneNumber,
                messageId = request.messageId,
                messageType = request.messageType
            )
        ).putAllData(
            buildChatData(
                chatId = request.topic,
                senderId = request.senderPhoneNumber,
                messageId = request.messageId,
                messageType = request.messageType,
            )
        ).build()
        return sendMessageAsync(message)
    }
}
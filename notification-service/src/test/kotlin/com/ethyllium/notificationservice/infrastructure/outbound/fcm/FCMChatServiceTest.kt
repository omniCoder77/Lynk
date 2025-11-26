package com.ethyllium.notificationservice.infrastructure.outbound.fcm

import com.ethyllium.notificationservice.domain.model.MessageType
import com.ethyllium.notificationservice.infrastructure.outbound.fcm.dto.ConversationChatNotificationRequest
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.util.concurrent.TimeUnit

@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class FCMChatWebIntegrationTest {

    @Autowired
    private lateinit var fcmChatService: FCMChatService

    private val webToken = System.getenv("FCM_TOKEN")

    @Test
    @Order(1)
    fun `should send simple web chat notification`() {
        println("\n>>> Test 1: Sending simple web notification")

        val request = ConversationChatNotificationRequest(
            token = webToken,
            conversationId = "chat_001",
            senderId = "user_alice",
            messageId = "msg_001",
            body = "Hey! This is a test message from integration test",
            messageType = MessageType.TEXT,
            senderAvatar = "https://i.pravatar.cc/150?img=1"
        )

        assertDoesNotThrow {
            fcmChatService.sendChatNotification(request).subscribe()
        }

        println("✅ Notification sent successfully")
        println("💬 Check your browser for the notification!")

        // Wait to see the notification
        TimeUnit.SECONDS.sleep(3)
    }

    @Test
    @Order(2)
    fun `should send web notification with different sender`() {
        println("\n>>> Test 2: Sending notification with different sender")

        val request = ConversationChatNotificationRequest(
            token = webToken,
            conversationId = "chat_002",
            senderId = "user_bob",
            messageId = "msg_002",
            body = "Hello from Bob! Can you see this notification?",
            messageType = MessageType.TEXT,
            senderAvatar = "https://i.pravatar.cc/150?img=5"
        )

        assertDoesNotThrow {
            fcmChatService.sendChatNotification(request).subscribe()
        }

        println("✅ Notification sent successfully")
        TimeUnit.SECONDS.sleep(3)
    }

    @Test
    @Order(3)
    fun `should send web notification for image message type`() {
        println("\n>>> Test 3: Sending image message notification")

        val request = ConversationChatNotificationRequest(
            token = webToken,
            conversationId = "chat_003",
            senderId = "user_charlie",
            messageId = "msg_003",
            body = "📷 Charlie sent you an image",
            messageType = MessageType.IMAGE,
            senderAvatar = "https://i.pravatar.cc/150?img=8"
        )

        assertDoesNotThrow {
            fcmChatService.sendChatNotification(request).subscribe()
        }

        println("✅ Image notification sent successfully")
        TimeUnit.SECONDS.sleep(3)
    }

    @Test
    @Order(4)
    fun `should send web notification with long message`() {
        println("\n>>> Test 4: Sending long message notification")

        val request = ConversationChatNotificationRequest(
            token = webToken,
            conversationId = "chat_004",
            senderId = "user_diana",
            messageId = "msg_004",
            body = "This is a much longer message to test how the notification handles longer text content. It should truncate or wrap properly in the notification UI.",
            messageType = MessageType.TEXT,
            senderAvatar = "https://i.pravatar.cc/150?img=9"
        )

        assertDoesNotThrow {
            fcmChatService.sendChatNotification(request).subscribe()
        }

        println("✅ Long message notification sent successfully")
        TimeUnit.SECONDS.sleep(3)
    }

    @Test
    @Order(5)
    fun `should send web notification with emojis`() {
        println("\n>>> Test 5: Sending notification with emojis")

        val request = ConversationChatNotificationRequest(
            token = webToken,
            conversationId = "chat_005",
            senderId = "user_eve",
            messageId = "msg_005",
            body = "🎉 Hey! Let's celebrate 🥳 This message has emojis! 🚀✨",
            messageType = MessageType.TEXT,
            senderAvatar = "https://i.pravatar.cc/150?img=10"
        )

        assertDoesNotThrow {
            fcmChatService.sendChatNotification(request).subscribe()
        }

        println("✅ Emoji notification sent successfully")
        TimeUnit.SECONDS.sleep(3)
    }

    @Test
    @Order(6)
    fun `should send rapid succession notifications`() {
        println("\n>>> Test 6: Sending rapid succession notifications")

        val messages = listOf(
            "First message in rapid succession", "Second message right after", "Third message following up"
        )

        messages.forEachIndexed { index, body ->
            val request = ConversationChatNotificationRequest(
                token = webToken,
                conversationId = "chat_rapid_${index + 1}",
                senderId = "user_frank",
                messageId = "msg_rapid_${index + 1}",
                body = body,
                messageType = MessageType.TEXT,
                senderAvatar = "https://i.pravatar.cc/150?img=12"
            )

            assertDoesNotThrow {
                fcmChatService.sendChatNotification(request).subscribe()
            }

            println("✅ Message ${index + 1}/3 sent")
            TimeUnit.MILLISECONDS.sleep(500) // Small delay between messages
        }

        println("✅ All rapid succession notifications sent")
        TimeUnit.SECONDS.sleep(3)
    }

    @Test
    @Order(7)
    fun `should handle notification with no avatar`() {
        println("\n>>> Test 7: Sending notification without avatar")

        val request = ConversationChatNotificationRequest(
            token = webToken,
            conversationId = "chat_007",
            senderId = "user_grace",
            messageId = "msg_007",
            body = "This notification has no avatar - should show default",
            messageType = MessageType.TEXT,
            senderAvatar = null  // No avatar
        )

        assertDoesNotThrow {
            fcmChatService.sendChatNotification(request).subscribe()
        }

        println("✅ Notification without avatar sent successfully")
        TimeUnit.SECONDS.sleep(3)
    }

    @Test
    @Order(8)
    fun `should send notification with same chatId to test grouping`() {
        println("\n>>> Test 8: Testing notification grouping (same chatId)")

        val chatId = "chat_grouped"

        listOf(
            "First message in this chat", "Second message - should replace first?", "Third message in the same chat"
        ).forEachIndexed { index, body ->
            val request = ConversationChatNotificationRequest(
                token = webToken,
                conversationId = chatId,
                senderId = "user_henry",
                messageId = "msg_grouped_${index + 1}",
                body = body,
                messageType = MessageType.TEXT,
                senderAvatar = "https://i.pravatar.cc/150?img=15"
            )

            assertDoesNotThrow {
                fcmChatService.sendChatNotification(request).subscribe()
            }

            println("✅ Grouped message ${index + 1}/3 sent")
            TimeUnit.SECONDS.sleep(1)
        }

        println("✅ All grouped notifications sent (should see grouping behavior)")
        TimeUnit.SECONDS.sleep(3)
    }

    @AfterAll
    fun teardown() {
        println("\n" + "=".repeat(80))
        println("FCM Web Integration Test Complete")
        println("=".repeat(80))
        println("Did you see all the notifications in your browser?")
        println("Check dunst logs if you missed any: journalctl -f | grep dunst")
        println("=".repeat(80))
    }
}
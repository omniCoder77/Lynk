package com.ethyllium.messageservice.application.service

import com.ethyllium.messageservice.application.dto.MessageRequest
import com.ethyllium.messageservice.domain.model.*
import com.ethyllium.messageservice.domain.port.inbound.MessageService
import com.ethyllium.messageservice.domain.port.outbound.ConversationRepository
import com.ethyllium.messageservice.domain.port.outbound.MessageByConversationRepository
import com.ethyllium.messageservice.domain.port.outbound.MessageByUserRepository
import com.ethyllium.messageservice.infrastructure.adapter.inbound.websocket.dto.ReceiverMessage
import com.ethyllium.messageservice.infrastructure.adapter.outbound.persistence.cassandra.entity.MessageByUserEntity
import com.ethyllium.messageservice.infrastructure.adapter.outbound.persistence.cassandra.entity.MessageByUserKey
import com.ethyllium.messageservice.infrastructure.adapter.outbound.persistence.cassandra.utils.BucketingUtil
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import java.time.Instant
import java.util.*

@Service
class MessageServiceImpl(
    private val conversationRepository: ConversationRepository,
    private val messageByUserRepository: MessageByUserRepository,
    private val messageByConversationRepository: MessageByConversationRepository,
    private val simpMessagingTemplate: SimpMessagingTemplate
) : MessageService {

    private val logger = LoggerFactory.getLogger(this::class.java)

    override fun sendMessage(senderId: String, request: MessageRequest): Mono<Pair<String, Long>> {
        val createdAt = Instant.now()
        val messageId = UUID.randomUUID().toString()
        val recipientId = request.recipientId
        val members = setOf(senderId, recipientId)

        // Find conversation or create a new one if it doesn't exist.
        return conversationRepository.findById(request.conversationId).switchIfEmpty(Mono.fromSupplier {
                Conversation(
                    conversationId = ConversationId(request.conversationId),
                    type = ConversationType.PRIVATE,
                    lastMessageId = MessageId("initial-private-message")
                )
            }.flatMap { newConversation ->
                conversationRepository.insert(newConversation).thenReturn(newConversation)
            }).flatMap { conversation ->
                val messageToSave = Message(
                    id = MessageId(messageId),
                    conversationId = conversation.conversationId,
                    senderId = UserId(senderId),
                    content = MessageContent(request.content),
                    messageType = request.messageType,
                    conversationType = conversation.type,
                    recipientId = UserId(recipientId),
                    createdAt = createdAt
                )

                // Define persistence operations
                val saveToConversationTable = messageByConversationRepository.insert(messageToSave)

                val saveToUserTables = members.map { memberId ->
                    val bucket = BucketingUtil.calculateBucket(memberId, createdAt)
                    val userMessageEntity = messageToSave.toMessageByUserEntity(bucket, memberId)
                    messageByUserRepository.insert(userMessageEntity)
                }

                val updateConversation =
                    conversationRepository.update(conversation.conversationId.value, "last_message_id", messageId)

                // Execute all writes. On success, push real-time notification.
                Mono.`when`(listOf(saveToConversationTable) + saveToUserTables + listOf(updateConversation))
                    .doOnSuccess {
                        logger.info("Message $messageId saved. Pushing to recipient $recipientId.")
                        val notification = ReceiverMessage(content = request.content, sender = senderId)
                        simpMessagingTemplate.convertAndSendToUser(recipientId, "/queue/private", notification)
                    }.thenReturn(Pair(messageId, createdAt.epochSecond))
            }
    }

    // Other methods remain simple for now...
    override fun editMessage(userId: String, messageId: String, content: String, createdAt: Long): Mono<Boolean> {
        return Mono.just(false)
    }

    override fun deleteMessage(userId: String, messageId: String, createdAt: Long): Mono<Boolean> {
        return Mono.just(false)
    }

    // This can be removed or left as is, as it's not a primary feature yet.
    override fun createGroupConversation(
        creatorId: String,
        name: String,
        initialMembers: Set<String>
    ): Mono<Conversation> {
        return Mono.error(NotImplementedError("Group functionality not implemented in this version."))
    }

    override fun getUserMessages(
        userId: String,
        conversationId: String?,
        days: Int,
        pageable: Pageable
    ): Page<Message> {
        val messages = messageByUserRepository.getUserMessages(userId, conversationId, days, pageable)
        val paged = messages.drop(pageable.offset.toInt()).take(pageable.pageSize)
        return PageImpl(paged, pageable, messages.size.toLong())
    }
}

// Add this helper if it's not in the entity file
fun Message.toMessageByUserEntity(
    bucket: Int,
    userId: String
): MessageByUserEntity {
    return MessageByUserEntity(
        key = MessageByUserKey(
            bucket,
            userId,
            this.createdAt,
            this.id.value
        ),
        content = this.content.value,
        message_type = this.messageType.name,
        conversation_type = this.conversationType.name,
        conversation_id = this.conversationId.value,
        recipient_id = if (userId != this.senderId.value) this.senderId.value else this.recipientId?.value
    )
}
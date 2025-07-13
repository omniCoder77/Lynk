package com.ethyllium.messageservice.application.service

import com.ethyllium.messageservice.application.dto.MessageRequest
import com.ethyllium.messageservice.domain.exception.MessageNotFoundException
import com.ethyllium.messageservice.domain.exception.OperationNotAuthorizedException
import com.ethyllium.messageservice.domain.model.*
import com.ethyllium.messageservice.domain.port.inbound.MessageService
import com.ethyllium.messageservice.domain.port.outbound.ConversationRepository
import com.ethyllium.messageservice.domain.port.outbound.MessageByConversationRepository
import com.ethyllium.messageservice.domain.port.outbound.MessageByUserRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.switchIfEmpty
import java.time.Instant
import java.util.*

@Component
class MessageServiceImpl(
    private val conversationRepository: ConversationRepository,
    private val messageByUserRepository: MessageByUserRepository,
    private val messageByConversationRepository: MessageByConversationRepository,
) : MessageService {

    override fun sendMessage(senderId: String, request: MessageRequest): Mono<Pair<String, Long>> {
        val createdAt = Instant.now()
        val members = setOf(senderId, request.recipientId)

        return conversationRepository.findById(request.conversationId).switchIfEmpty(
            Mono.just(
                Conversation(
                    conversationId = ConversationId(UUID.randomUUID().toString()),
                    type = ConversationType.valueOf(request.conversationType.name),
                    lastMessageId = MessageId(UUID.randomUUID().toString()),
                )
            )
        ).flatMap { conversation ->
            val messageMono = messageByConversationRepository.insert(
                Message(
                    id = MessageId(conversation.lastMessageId.value),
                    conversationId = conversation.conversationId,
                    senderId = UserId(senderId),
                    content = MessageContent(request.content),
                    messageType = request.messageType,
                    conversationType = request.conversationType,
                    recipientId = UserId(request.recipientId),
                    fileUrl = request.fileUrl?.let { FileUrl(request.fileUrl) },
                    createdAt = createdAt,
                    editedAt = null,
                    isDeleted = false
                )
            )
            val userMessagesFlux = members.map { member ->
                Message(
                    id = MessageId(conversation.lastMessageId.value),
                    conversationId = conversation.conversationId,
                    senderId = UserId(senderId),
                    content = MessageContent(request.content),
                    messageType = request.messageType,
                    conversationType = request.conversationType,
                    recipientId = if (request.conversationType == ConversationType.PRIVATE && member != senderId) UserId(
                        senderId
                    ) else null,
                    fileUrl = request.fileUrl?.let { FileUrl(request.fileUrl) },
                    createdAt = createdAt,
                    editedAt = null,
                    isDeleted = false
                )
            }

            val updateLastMessageId = conversationRepository.update(
                conversation.conversationId.value, "last_message_id", MessageId(conversation.lastMessageId.value)
            )

            Mono.`when`(
                messageMono, messageByUserRepository.insertAll(userMessagesFlux), updateLastMessageId
            ).thenReturn(Pair(conversation.lastMessageId.value, createdAt.epochSecond))
        }
    }

    override fun editMessage(
        userId: String, messageId: String, content: String, createdAt: Long
    ): Mono<Boolean> {
        return messageByUserRepository.findById(userId, createdAt, messageId)
            .switchIfEmpty { Mono.error(MessageNotFoundException("Message Not Found")) }.flatMap {
                if (it.senderId.value == userId) {
                    messageByUserRepository.update(userId, "content", content)
                } else {
                    Mono.error(OperationNotAuthorizedException("You are not authorized to edit this message"))
                }
            }
    }

    override fun deleteMessage(userId: String, messageId: String, createdAt: Long): Mono<Boolean> {
        return messageByUserRepository.findById(userId, createdAt, messageId)
            .switchIfEmpty { Mono.error(MessageNotFoundException("Message Not Found")) }.flatMap {
                messageByUserRepository.deleteById(userId, createdAt, messageId).map { it != null }
            }
    }

    override fun getUserMessages(
        userId: String, conversationId: String?, days: Int, pageable: Pageable
    ): Page<Message> {
        val messages = messageByUserRepository.getUserMessages(userId, conversationId, days, pageable)
        val total = messages.size
        val paged = messages.drop(pageable.offset.toInt()).take(pageable.pageSize)
        return PageImpl(paged, pageable, total.toLong())
    }
}
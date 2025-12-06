package com.ethyllium.userservice.infrastructure.inbound.grpc

import com.ethyllium.userservice.domain.port.driven.BlocklistRepository
import com.ethyllium.userservice.domain.port.driven.ConversationRepository
import com.ethyllium.userservice.infrastructure.util.UUIDUtils
import com.ethyllium.userservice.infrastructure.web.grpc.ConversationValidationRequest
import com.ethyllium.userservice.infrastructure.web.grpc.ConversationValidationResponse
import com.ethyllium.userservice.infrastructure.web.grpc.ConversationValidationStatus
import com.ethyllium.userservice.infrastructure.web.grpc.ValidationServiceGrpcKt
import kotlinx.coroutines.async
import kotlinx.coroutines.reactor.awaitSingleOrNull
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory
import org.springframework.grpc.server.service.GrpcService
import java.util.*

@GrpcService
class ValidationService(
    private val conversationRepository: ConversationRepository,
    private val blocklistRepository: BlocklistRepository,
) : ValidationServiceGrpcKt.ValidationServiceCoroutineImplBase() {
    private val logger = LoggerFactory.getLogger(this::class.java)
    override suspend fun validateConversation(request: ConversationValidationRequest): ConversationValidationResponse =
        withContext(context) {
            try {
                UUID.fromString(request.senderId)
                UUID.fromString(request.recipientId)
            } catch (e: IllegalArgumentException) {
                logger.warn("Invalid UUID format in conversation validation", e)
                return@withContext ConversationValidationResponse.newBuilder()
                    .setStatus(ConversationValidationStatus.CONVERSATION_VALIDATION_STATUS_BAD_FORMAT).build()
            }

            val conversationId = UUIDUtils.merge(request.senderId, request.recipientId)
            val senderBlockedRecipientId = UUIDUtils.merge(request.senderId, request.recipientId, false)
            val recipientBlockedSenderId = UUIDUtils.merge(request.recipientId, request.senderId, false)

            val conversation = async { conversationRepository.select(conversationId).awaitSingleOrNull() }.await()
            val blocklist1 = async { blocklistRepository.getBlocklistById(senderBlockedRecipientId).awaitSingleOrNull() }.await()
            val blocklist2 = async { blocklistRepository.getBlocklistById(recipientBlockedSenderId).awaitSingleOrNull() }.await()

            return@withContext when {
                conversation == null -> ConversationValidationResponse.newBuilder()
                    .setStatus(ConversationValidationStatus.CONVERSATION_VALIDATION_STATUS_NOT_FOUND).build()

                blocklist1 != null -> ConversationValidationResponse.newBuilder()
                    .setStatus(ConversationValidationStatus.CONVERSATION_VALIDATION_STATUS_SENDER_BLOCKED_RECIPIENT)
                    .build()

                blocklist2 != null -> ConversationValidationResponse.newBuilder()
                    .setStatus(ConversationValidationStatus.CONVERSATION_VALIDATION_STATUS_RECIPIENT_BLOCKED_SENDER)
                    .build()

                else -> ConversationValidationResponse.newBuilder()
                    .setStatus(ConversationValidationStatus.CONVERSATION_VALIDATION_STATUS_OK).build()
            }
        }
}
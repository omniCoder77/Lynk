package com.ethyllium.userservice.infrastructure.inbound.grpc

import com.ethyllium.userservice.domain.port.driven.ConversationRepository
import com.ethyllium.userservice.domain.port.driven.MemberRepository
import com.ethyllium.userservice.domain.port.driven.RoomRepository
import com.ethyllium.userservice.infrastructure.util.UUIDUtils
import com.ethyllium.userservice.infrastructure.web.grpc.*
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.reactor.awaitSingleOrNull
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory
import org.springframework.grpc.server.service.GrpcService
import java.util.*

@GrpcService
class ValidationService(
    private val conversationRepository: ConversationRepository,
    private val roomRepository: RoomRepository,
    private val memberRepository: MemberRepository
) : ValidationServiceGrpcKt.ValidationServiceCoroutineImplBase() {
    private val logger = LoggerFactory.getLogger(this::class.java)
    override suspend fun validateConversation(request: ConversationValidationRequest): ConversationValidationResponse {
        try {
            UUID.fromString(request.senderId)
            UUID.fromString(request.recipientId)
        } catch (e: IllegalArgumentException) {
            logger.warn("Invalid UUID format in conversation validation", e)
            return ConversationValidationResponse.newBuilder()
                .setStatus(ConversationValidationStatus.CONVERSATION_VALIDATION_STATUS_BAD_FORMAT).build()
        }

        val conversationId = UUIDUtils.getConversationId(request.senderId, request.recipientId)

        val conversation = conversationRepository.select(conversationId).awaitSingleOrNull()

        return when {
            conversation == null -> ConversationValidationResponse.newBuilder()
                .setStatus(ConversationValidationStatus.CONVERSATION_VALIDATION_STATUS_NOT_FOUND).build()

            conversation.isBlocked -> ConversationValidationResponse.newBuilder()
                .setStatus(ConversationValidationStatus.CONVERSATION_VALIDATION_STATUS_BLOCKED).build()

            else -> ConversationValidationResponse.newBuilder()
                .setStatus(ConversationValidationStatus.CONVERSATION_VALIDATION_STATUS_OK).build()
        }
    }

    override suspend fun validateRoom(request: RoomValidationRequest): RoomValidationResponse = withContext(context) {
        coroutineScope {
            val memberId = UUID.fromString(request.senderId)
            val roomId = UUID.fromString(request.roomId)

            val roomDeferred = async { roomRepository.get(roomId).awaitSingleOrNull() }
            val memberDeferred = async { memberRepository.get(memberId).awaitSingleOrNull() }

            val room = roomDeferred.await()
            val member = memberDeferred.await()

            when {
                room == null -> RoomValidationResponse.newBuilder()
                    .setStatus(RoomValidationStatus.ROOM_VALIDATION_STATUS_ROOM_NOT_FOUND).build()

                member == null -> RoomValidationResponse.newBuilder()
                    .setStatus(RoomValidationStatus.ROOM_VALIDATION_STATUS_SENDER_NOT_MEMBER).build()

                !member.isAllowedToSendMedia -> RoomValidationResponse.newBuilder()
                    .setStatus(RoomValidationStatus.ROOM_VALIDATION_STATUS_SENDER_BLOCKED_FROM_ROOM).build()

                else -> RoomValidationResponse.newBuilder().setStatus(RoomValidationStatus.ROOM_VALIDATION_STATUS_OK)
                    .build()
            }
        }
    }
}
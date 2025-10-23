package com.lynk.messageservice.application.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.lynk.messageservice.domain.exception.DuplicateRoomException
import com.lynk.messageservice.domain.exception.RoomUnauthorizedException
import com.lynk.messageservice.domain.model.Room
import com.lynk.messageservice.domain.model.RoomMember
import com.lynk.messageservice.domain.model.RoomRole
import com.lynk.messageservice.domain.port.driven.CuckooFilter
import com.lynk.messageservice.domain.port.driven.RoomService
import com.lynk.messageservice.domain.port.driver.MemberByRoomRepository
import com.lynk.messageservice.domain.port.driver.RoomByMemberRepository
import com.lynk.messageservice.domain.port.driver.RoomMessageRepository
import com.lynk.messageservice.infrastructure.inbound.web.dto.response.toResponse
import com.lynk.messageservice.infrastructure.outbound.notification.push.FCMService
import com.lynk.messageservice.infrastructure.outbound.notification.push.NotificationRequest
import com.lynk.messageservice.infrastructure.outbound.persistence.cassandra.entity.RoomMessage
import com.lynk.messageservice.infrastructure.outbound.persistence.cassandra.entity.RoomMessageKey
import org.slf4j.LoggerFactory
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.Instant
import java.util.*

@Service

class RoomServiceImpl(
    private val memberByRoomRepository: MemberByRoomRepository,
    private val roomByMemberRepository: RoomByMemberRepository,
    private val roomMessageRepository: RoomMessageRepository,
    private val cuckooFilter: CuckooFilter,
    private val fcmService: FCMService,
    private val presenceService: PresenceService,
    private val reactiveRedisTemplate: ReactiveRedisTemplate<String, String>
) : RoomService {

    private val logger = LoggerFactory.getLogger(this::class.java)
    private val objectMapper = ObjectMapper().registerModule(KotlinModule.Builder().build()).registerModule(
        JavaTimeModule()
    )

    override fun createRoom(
        name: String, description: String?, creatorId: UUID, initialMemberIds: List<UUID>
    ): Mono<UUID> {
        val roomId = UUID.randomUUID()
        val allMemberIds = (initialMemberIds + creatorId).distinct()
        if (cuckooFilter.exists("rooms_name", name)) {
            return Mono.error(DuplicateRoomException("Room with the same name already exists"))
        } else {
            cuckooFilter.add("rooms_name", name)
        }

        val memberCreationMonos = allMemberIds.map { memberId ->
            val role = if (memberId == creatorId) RoomRole.ADMIN else RoomRole.MEMBER

            memberByRoomRepository.createMemberByRoom(roomId, memberId, role, name, description)
                .then(roomByMemberRepository.createRoomByMember(memberId, roomId, name))
        }

        return Flux.concat(memberCreationMonos).then(Mono.just(roomId))
    }

    override fun updateRoomDetails(
        roomId: UUID, name: String?, description: String?, avatarUrl: String?, updater: String
    ): Mono<Boolean> {
        return memberByRoomRepository.getMemberById(UUID.fromString(updater), roomId)
            .switchIfEmpty(Mono.error(RoomUnauthorizedException("Updater is not a member of the room")))
            .flatMap { updaterMembership ->
                if (updaterMembership.role != RoomRole.ADMIN.name) {
                    Mono.error(RoomUnauthorizedException("Only admins can update room details"))
                } else {
                    val updaterUUID = UUID.fromString(updater)
                    memberByRoomRepository.updateRoom(description, avatarUrl, name, roomId, updaterUUID)
                        .flatMap { memberId ->
                            roomByMemberRepository.updateRoomByMember(
                                memberId, roomId, null, null, avatarUrl, name
                            )
                        }.all { it }
                }
            }
    }

    override fun addMemberToRoom(roomId: UUID, memberId: UUID, role: RoomRole, inviterId: UUID): Mono<Boolean> {
        return memberByRoomRepository.getMemberById(inviterId, roomId)
            .switchIfEmpty(Mono.error(RoomUnauthorizedException("Inviter not found in room"))).flatMap {
                if (it.role != RoomRole.ADMIN.name) {
                    Mono.error(RoomUnauthorizedException("Only admins can add members"))
                } else {
                    memberByRoomRepository.createMemberByRoom(
                        roomId = roomId,
                        memberId = memberId,
                        role = role,
                        displayName = it.displayName,
                        description = it.description,
                    )
                }
            }
    }

    override fun getRoomMembers(roomId: UUID): Flux<RoomMember> {
        return memberByRoomRepository.getMembersByRoomId(roomId)
    }


    override fun sendMessage(
        roomId: UUID, senderId: UUID, content: String, replyToMessageId: UUID?, timestamp: Instant
    ): Mono<Boolean> {
        val messageId = UUID.randomUUID()

        val roomMessage = RoomMessage(
            key = RoomMessageKey(
                roomId = roomId, timestamp = timestamp, messageId = messageId
            ), senderId = senderId, content = content, replyToMessageId = replyToMessageId
        )

        return memberByRoomRepository.getMemberById(senderId, roomId)
            .switchIfEmpty(Mono.error(RoomUnauthorizedException("Sender is not a member of this room")))
            .flatMap { senderInfo ->
                roomMessageRepository.saveMessage(roomId, content, senderId, messageId, replyToMessageId, timestamp)
                    .flatMap { success ->
                        if (success) {
                            val messageResponse = roomMessage.toResponse()
                            val payload = objectMapper.writeValueAsString(messageResponse)
                            val channel = "room-messages:${roomId}"

                            reactiveRedisTemplate.convertAndSend(channel, payload)
                                .doOnSuccess { logger.info("Published message to Redis channel: $channel") }
                                .then(dispatchOfflineNotifications(roomMessage, senderInfo.displayName))
                                .then(Mono.just(true))
                        } else {
                            Mono.just(false)
                        }
                    }
            }
    }

    private fun dispatchOfflineNotifications(message: RoomMessage, senderName: String): Mono<Void> {
        return getRoomMembers(message.key.roomId).filter { it.memberId != message.senderId }.flatMap { member ->
            presenceService.isUserOnline(member.memberId).flatMap { isOnline ->
                if (!isOnline) {
                    logger.info("User ${member.memberId} is offline. Sending FCM notification.")
                    sendFcm(member.memberId, senderName, message.content)
                } else {
                    Mono.empty()
                }
            }
        }.then()
    }

    private fun sendFcm(recipientId: UUID, senderName: String, messageContent: String): Mono<Void> {
        val userDeviceToken = "placeholder_fcm_token_for_user_${recipientId}"

        val request = NotificationRequest(
            title = "New message from $senderName",
            body = messageContent,
            token = userDeviceToken,
            topic = "chat_messages"
        )

        return Mono.fromRunnable {
            try {
                fcmService.sendMessageToToken(request)
            } catch (e: Exception) {
                logger.error("Failed to send FCM to user $recipientId", e)
            }
        }
    }


    override fun getMessages(roomId: UUID, start: Instant, end: Instant): Flux<RoomMessage> {
        return roomMessageRepository.getMessagesByRoomId(roomId, start, end)
    }

    override fun getRooms(memberId: UUID): Flux<Room> {
        return roomByMemberRepository.getRooms(memberId)
    }
}
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
import com.lynk.messageservice.domain.port.driven.EventPublisher
import com.lynk.messageservice.domain.port.driven.RoomService
import com.lynk.messageservice.domain.port.driver.MemberByRoomRepository
import com.lynk.messageservice.domain.port.driver.RoomByMemberRepository
import com.lynk.messageservice.domain.port.driver.RoomMessageRepository
import com.lynk.messageservice.infrastructure.inbound.web.dto.response.toResponse
import com.lynk.messageservice.infrastructure.outbound.kafka.dto.RoomMessageEvent
import com.lynk.messageservice.infrastructure.outbound.kafka.dto.RoomMessagePayload
import com.lynk.messageservice.infrastructure.outbound.persistence.cassandra.entity.RoomMessage
import com.lynk.messageservice.infrastructure.outbound.persistence.cassandra.entity.RoomMessageKey
import org.slf4j.LoggerFactory
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
    private val eventPublisher: EventPublisher
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
                        displayName = it.display_name,
                        description = it.description,
                    )
                }
            }
    }

    override fun getRoomMembers(roomId: UUID): Flux<RoomMember> {
        return memberByRoomRepository.getMembersByRoomId(roomId)
    }

    override fun sendMessage(
        roomId: UUID, senderId: UUID, content: String, replyToMessageId: UUID?, timestamp: Instant, phoneNumber: String
    ): Mono<Boolean> {
        val messageId = UUID.randomUUID()

        val roomMessage = RoomMessage(
            key = RoomMessageKey(
                roomId = roomId, timestamp = timestamp, messageId = messageId
            ), sender_id = senderId, content = content, reply_to_message_id = replyToMessageId
        )

        return memberByRoomRepository.getMemberById(senderId, roomId)
            .switchIfEmpty(Mono.error(RoomUnauthorizedException("Sender is not a member of this room"))).flatMap {
                roomMessageRepository.saveMessage(roomId, content, senderId, messageId, replyToMessageId, timestamp)
                    .flatMap { success ->
                        if (success) {
                            val messageResponse = roomMessage.toResponse()
                            objectMapper.writeValueAsString(messageResponse)
                            val roomMessageEvent = RoomMessageEvent(
                                messageId = roomMessage.key.messageId, payload = RoomMessagePayload(
                                    content = roomMessage.content,
                                    fileUrls = null,
                                    roomId = roomId,
                                    senderPhoneNumber = phoneNumber,
                                    senderId = senderId
                                )
                            )
                            Mono.fromRunnable { (eventPublisher.publish(roomMessageEvent)) }
                        } else {
                            Mono.just(false)
                        }
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
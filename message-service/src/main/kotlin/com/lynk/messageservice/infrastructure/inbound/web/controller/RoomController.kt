package com.lynk.messageservice.infrastructure.inbound.web.controller

import com.lynk.messageservice.domain.model.Room
import com.lynk.messageservice.domain.port.driven.RoomService
import com.lynk.messageservice.infrastructure.inbound.web.dto.request.AddRoomMemberRequest
import com.lynk.messageservice.infrastructure.inbound.web.dto.request.CreateRoomRequest
import com.lynk.messageservice.infrastructure.inbound.web.dto.request.SendMessageRequest
import com.lynk.messageservice.infrastructure.inbound.web.dto.request.UpdateRoomRequest
import com.lynk.messageservice.infrastructure.inbound.web.dto.response.MessageResponse
import com.lynk.messageservice.infrastructure.inbound.web.dto.response.RoomMemberResponse
import com.lynk.messageservice.infrastructure.inbound.web.dto.response.toResponse
import com.lynk.messageservice.infrastructure.outbound.security.LynkAuthenticationToken
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.security.Principal
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.*

@RestController
@RequestMapping("/api/v1/rooms")
class RoomController(private val roomService: RoomService) {

    @PostMapping
    fun createRoom(
        @Valid @RequestBody request: CreateRoomRequest, authentication: Authentication
    ): Mono<ResponseEntity<Map<String, UUID>>> {
        val creatorId = try { UUID.fromString(authentication.name) } catch (e: IllegalArgumentException) {
            return Mono.just(ResponseEntity.status(HttpStatus.FORBIDDEN).build())
        }
        return roomService.createRoom(request.name, request.description, creatorId, request.initialMemberIds)
            .map { roomId ->
                ResponseEntity.status(HttpStatus.CREATED).body(mapOf("roomId" to roomId))
            }
    }

    @GetMapping
    fun getRooms(authentication: Authentication): Flux<Room> {
        val userId = UUID.fromString(authentication.name)
        return roomService.getRooms(userId)
    }

    @PutMapping("/{roomId}")
    fun updateRoom(
        @Valid @PathVariable roomId: UUID, @RequestBody request: UpdateRoomRequest, authentication: Authentication
    ): Mono<ResponseEntity<Void>> {
        return roomService.updateRoomDetails(roomId, request.name, request.description, request.avatarUrl, authentication.name)
            .map { ResponseEntity.ok().build() }
    }

    @GetMapping("/{roomId}/members")
    fun getRoomMembers(@Valid @PathVariable roomId: UUID): Mono<ResponseEntity<List<RoomMemberResponse>>> {
        return roomService.getRoomMembers(roomId).map { it.toResponse() }.collectList().map { ResponseEntity.ok(it) }
    }

    @PostMapping("/{roomId}/members")
    fun addMember(
        @Valid @PathVariable roomId: UUID,@Valid @RequestBody request: AddRoomMemberRequest, principal: Principal
    ): Mono<ResponseEntity<Void>> {
        val inviterId = UUID.fromString(principal.name)
        return roomService.addMemberToRoom(roomId, request.memberId, request.role, inviterId)
            .map { ResponseEntity.status(HttpStatus.CREATED).build() }
    }

    @GetMapping("/{roomId}/messages")
    fun getMessages(
        @PathVariable roomId: UUID,
        @RequestParam(required = false) start: Instant?,
        @RequestParam(required = false) end: Instant?
    ): Mono<ResponseEntity<List<MessageResponse>>> {
        val endTime = end ?: Instant.now()
        val startTime = start ?: endTime.minus(30, ChronoUnit.DAYS)

        return roomService.getMessages(roomId, startTime, endTime).map { it.toResponse() }.collectList()
            .map { ResponseEntity.ok(it) }
    }

    @PostMapping("/{roomId}/messages")
    fun sendMessage(
        @PathVariable roomId: UUID,@Valid @RequestBody request: SendMessageRequest, principal: LynkAuthenticationToken
    ): Mono<ResponseEntity<Void>> {
        val senderId = UUID.fromString(principal.name)
        return roomService.sendMessage(roomId, senderId, request.content, request.replyToMessageId, request.timestamp, principal.phoneNumber).map { success ->
                if (success) {
                    ResponseEntity.status(HttpStatus.CREATED).build()
                } else {
                    ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
                }
            }
    }
}
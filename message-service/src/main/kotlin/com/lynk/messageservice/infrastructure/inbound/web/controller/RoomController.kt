package com.lynk.messageservice.infrastructure.inbound.web.controller

import com.lynk.messageservice.domain.port.driven.RoomService
import com.lynk.messageservice.infrastructure.inbound.web.dto.CreateRoomRequest
import com.lynk.messageservice.infrastructure.inbound.web.dto.UpdateRoomRequest
import com.lynk.messageservice.infrastructure.outbound.persistence.cassandra.entity.Room
import org.springframework.http.HttpStatus
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import reactor.core.publisher.Mono
import java.util.UUID

@RestController
@RequestMapping("/api/v1/rooms")
class RoomController(private val roomService: RoomService) {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createRoom(@RequestBody request: CreateRoomRequest, authentication: Authentication): Mono<Room> {
        val creatorId = UUID.fromString(authentication.name)
        return roomService.createRoom(
            request.name,
            creatorId,
            request.roomType,
            request.description,
            request.avatarUrl
        )
    }

    @GetMapping("/{roomId}")
    fun getRoom(@PathVariable roomId: UUID): Mono<Room> {
        return roomService.getRoomDetails(roomId)
            .switchIfEmpty(Mono.error(ResponseStatusException(HttpStatus.NOT_FOUND, "Room not found")))
    }

    @PutMapping("/{roomId}")
    fun updateRoom(
        @PathVariable roomId: UUID,
        @RequestBody request: UpdateRoomRequest,
        authentication: Authentication
    ): Mono<Room> {
        return roomService.updateRoom(
            roomId,
            request.name,
            request.description,
            request.avatarUrl,
            request.roomType
        ).switchIfEmpty(Mono.error(ResponseStatusException(HttpStatus.NOT_FOUND, "Room not found or update failed")))
    }

    @DeleteMapping("/{roomId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteRoom(@PathVariable roomId: UUID, authentication: Authentication): Mono<Void> {
        val callingUserId = UUID.fromString(authentication.name)
        return roomService.deleteRoom(roomId, callingUserId)
            .flatMap { success ->
                if (success) Mono.empty()
                else Mono.error(
                    ResponseStatusException(
                        HttpStatus.FORBIDDEN,
                        "Unauthorized to delete room or room not found"
                    )
                )
            }
    }
}
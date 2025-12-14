package com.ethyllium.roomservice.infrastructure.inbound.web

import com.ethyllium.roomservice.domain.model.Room
import com.ethyllium.roomservice.domain.port.driver.RoomService
import com.ethyllium.roomservice.infrastructure.inbound.web.dto.CreateRoomRequest
import com.ethyllium.roomservice.infrastructure.inbound.web.dto.UpdateRoomRequest
import com.ethyllium.roomservice.infrastructure.outbound.security.LynkAuthenticationToken
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono
import java.util.*

@RestController
@RequestMapping("/rooms")
class RoomController(private val roomService: RoomService) {

    private val logger = LoggerFactory.getLogger(this::class.java)

    @PostMapping
    fun createRoom(
        authenticationToken: LynkAuthenticationToken, @RequestBody createRoomRequest: CreateRoomRequest
    ): Mono<ResponseEntity<String>> {

        val creatorId = UUID.fromString(authenticationToken.userId)
        val room = Room(
            roomId = UUID.randomUUID(),
            name = createRoomRequest.roomName,
            maxSize = createRoomRequest.maxSize,
            visibility = createRoomRequest.visibility,
        )
        return roomService.create(room, creatorId).flatMap { success ->
            if (success) {
                Mono.just(ResponseEntity.status(HttpStatus.CREATED).body("Room created with id: ${room.roomId}"))
            } else {
                Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to create room"))
            }
        }
    }

    @PatchMapping
    fun updateRoom(
        authenticationToken: LynkAuthenticationToken,
        @RequestBody updateRoomRequest: UpdateRoomRequest
    ): Mono<ResponseEntity<String>> {
        if (updateRoomRequest.roomName == null && updateRoomRequest.maxSize == null && updateRoomRequest.visibility == null) {
            return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body("No fields to update"))
        }
        val updaterId = UUID.fromString(authenticationToken.userId)
        return roomService.update(
            updaterId,
            updateRoomRequest.roomName,
            updateRoomRequest.roomId,
            updateRoomRequest.maxSize,
            updateRoomRequest.visibility
        ).flatMap {
            if (it) {
                Mono.just(ResponseEntity.status(HttpStatus.OK).body("Room updated successfully"))
            } else {
                Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to update room"))
            }
        }
    }

    @DeleteMapping("/{roomId}")
    fun deleteRoom(
        authenticationToken: LynkAuthenticationToken, @PathVariable roomId: UUID
    ): Mono<ResponseEntity<String>> {
        val deleterId = UUID.fromString(authenticationToken.userId)
        return roomService.delete(deleterId, roomId).flatMap {
            if (it) {
                Mono.just(ResponseEntity.status(HttpStatus.OK).body("Room deleted successfully"))
            } else {
                Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to delete room"))
            }
        }
    }
}
package com.ethyllium.roomservice.infrastructure.inbound.web

import com.ethyllium.roomservice.domain.exception.RoomAlreadyExistsException
import com.ethyllium.roomservice.domain.model.Room
import com.ethyllium.roomservice.domain.port.driver.RoomService
import com.ethyllium.roomservice.infrastructure.inbound.web.dto.CreateRoomRequest
import com.ethyllium.roomservice.infrastructure.outbound.security.LynkAuthenticationToken
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
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
        }.onErrorResume { error ->
            when (error) {
                is RoomAlreadyExistsException -> Mono.just(
                    ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Room with room name ${room.name} already exists")
                )

                else -> Mono.just(ResponseEntity.internalServerError().build())
            }
        }
    }

}
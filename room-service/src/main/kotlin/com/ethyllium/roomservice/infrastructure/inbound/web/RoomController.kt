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
        try {
            UUID.fromString(authenticationToken.userId)
        } catch (e: IllegalArgumentException) {
            logger.error("Invalid userId, get a valid authentication token", e)
            return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).build())
        }

        val room = Room(
            roomId = UUID.randomUUID(),
            name = createRoomRequest.roomName,
            maxSize = createRoomRequest.maxSize,
            visibility = createRoomRequest.visibility,
        )
        return roomService.create(room).map {
            ResponseEntity.status(HttpStatus.CREATED).body("Room created with id: ${room.roomId}")
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
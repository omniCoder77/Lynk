package com.ethyllium.roomservice.application.service

import com.ethyllium.roomservice.domain.model.Room
import com.ethyllium.roomservice.domain.port.driven.RoomRepository
import com.ethyllium.roomservice.domain.port.driver.RoomService
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Service
class RoomServiceImpl(private val roomRepository: RoomRepository) : RoomService {
    override fun create(room: Room): Mono<Boolean> {
        return roomRepository.insert(room)
    }
}
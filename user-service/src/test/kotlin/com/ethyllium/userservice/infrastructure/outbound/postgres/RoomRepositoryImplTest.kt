package com.ethyllium.userservice.infrastructure.outbound.postgres

import com.ethyllium.userservice.domain.model.Room
import com.ethyllium.userservice.infrastructure.outbound.postgres.entity.RoomEntity
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.data.relational.core.query.Query
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import java.util.UUID

@ExtendWith(MockKExtension::class)
class RoomRepositoryImplTest {

    @MockK
    lateinit var r2dbcEntityTemplate: R2dbcEntityTemplate

    @InjectMockKs
    lateinit var roomRepository: RoomRepositoryImpl

    private val roomId = UUID.randomUUID()
    private val roomName = "General"

    private val room = Room(roomId, roomName)
    private val roomEntity = RoomEntity(roomId, roomName)

    @Test
    fun `store inserts room entity and returns roomId`() {
        every { r2dbcEntityTemplate.insert(any<RoomEntity>()) } returns Mono.just(roomEntity)

        StepVerifier.create(roomRepository.store(room))
            .expectNext(roomId)
            .verifyComplete()

        verify {
            r2dbcEntityTemplate.insert(match<RoomEntity> {
                it.roomId == roomId && it.roomName == roomName
            })
        }
    }

    @Test
    fun `delete removes entity and returns true on success`() {
        every {
            r2dbcEntityTemplate.delete(any<Query>(), RoomEntity::class.java)
        } returns Mono.just(1)

        StepVerifier.create(roomRepository.delete(roomId))
            .expectNext(true)
            .verifyComplete()
    }

    @Test
    fun `delete returns false when no entity deleted`() {
        every {
            r2dbcEntityTemplate.delete(any<Query>(), RoomEntity::class.java)
        } returns Mono.just(0)

        StepVerifier.create(roomRepository.delete(roomId))
            .expectNext(false)
            .verifyComplete()
    }

    @Test
    fun `get returns mapped Room when entity exists`() {
        every {
            r2dbcEntityTemplate.selectOne(any<Query>(), RoomEntity::class.java)
        } returns Mono.just(roomEntity)

        StepVerifier.create(roomRepository.get(roomId))
            .expectNext(room)
            .verifyComplete()
    }

    @Test
    fun `get completes empty when entity does not exist`() {
        every {
            r2dbcEntityTemplate.selectOne(any<Query>(), RoomEntity::class.java)
        } returns Mono.empty()

        StepVerifier.create(roomRepository.get(roomId))
            .verifyComplete()
    }
}
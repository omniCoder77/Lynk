package com.lynk.messageservice.infrastructure.outbound.persistence.cassandra

import com.lynk.messageservice.TestcontainersConfiguration
import com.lynk.messageservice.infrastructure.outbound.persistence.cassandra.entity.RoomByMember
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.data.cassandra.core.ReactiveCassandraTemplate
import org.testcontainers.junit.jupiter.Testcontainers
import reactor.test.StepVerifier
import java.util.UUID

@SpringBootTest
@Import(TestcontainersConfiguration::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Testcontainers
class RoomByMemberRepositoryImplTest {

    @Autowired
    private lateinit var roomByMemberRepository: RoomByMemberRepositoryImpl

    @Autowired
    private lateinit var reactiveCassandraTemplate: ReactiveCassandraTemplate

    private val memberId = UUID.randomUUID()
    private val roomId = UUID.randomUUID()

    @AfterEach
    fun cleanup() {
        reactiveCassandraTemplate.truncate(RoomByMember::class.java).block()
    }

    @Test
    fun `createRoomByMember should insert a new record successfully`() {
        val result = roomByMemberRepository.createRoomByMember(
            memberId = memberId,
            roomId = roomId,
            name = "Test Room",
            avatarExtension = "png"
        )

        StepVerifier.create(result)
            .expectNext(roomId)
            .verifyComplete()
    }

    @Test
    fun `createRoomByMember should handle null avatarExtension`() {
        val result = roomByMemberRepository.createRoomByMember(
            memberId = memberId,
            roomId = roomId,
            name = "Another Room",
            avatarExtension = null
        )

        StepVerifier.create(result)
            .expectNext(roomId)
            .verifyComplete()
    }

    @Test
    fun `updateRoomByMember should update all provided fields`() {
        roomByMemberRepository.createRoomByMember(memberId, roomId, "Original Name").block()

        val updateResult = roomByMemberRepository.updateRoomByMember(
            memberId = memberId,
            roomId = roomId,
            lastMessagePreview = "See you soon!",
            lastMessenger = "John Doe",
            avatarExtension = "jpg"
        )

        StepVerifier.create(updateResult)
            .expectNext(true)
            .verifyComplete()

        // Verify update
        val updatedRecord = reactiveCassandraTemplate.selectOneById(
            com.lynk.messageservice.infrastructure.outbound.persistence.cassandra.entity.RoomByMemberKey(memberId, roomId),
            RoomByMember::class.java
        )

        StepVerifier.create(updatedRecord)
            .verifyComplete()
    }

    @Test
    fun `updateRoomByMember should update only specified fields`() {
        roomByMemberRepository.createRoomByMember(memberId, roomId, "Original Name").block()

        val updateResult = roomByMemberRepository.updateRoomByMember(
            memberId = memberId,
            roomId = roomId,
            lastMessagePreview = "Just this one",
            lastMessenger = null,
            avatarExtension = null
        )

        StepVerifier.create(updateResult)
            .expectNext(true)
            .verifyComplete()

        // Verify update
        val updatedRecord = reactiveCassandraTemplate.selectOneById(
            com.lynk.messageservice.infrastructure.outbound.persistence.cassandra.entity.RoomByMemberKey(memberId, roomId),
            RoomByMember::class.java
        )

        StepVerifier.create(updatedRecord)
            .verifyComplete()
    }

    @Test
    fun `updateRoomByMember should complete successfully for non-existent record`() {
        val updateResult = roomByMemberRepository.updateRoomByMember(
            memberId = UUID.randomUUID(),
            roomId = UUID.randomUUID(),
            lastMessagePreview = "This won't be saved"
        )

        StepVerifier.create(updateResult)
            .expectNext(true)
            .verifyComplete()
    }
}
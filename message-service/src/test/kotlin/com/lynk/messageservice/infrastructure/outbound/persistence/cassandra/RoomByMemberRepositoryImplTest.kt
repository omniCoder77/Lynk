package com.lynk.messageservice.infrastructure.outbound.persistence.cassandra

import com.lynk.messageservice.infrastructure.outbound.persistence.cassandra.entity.RoomByMember
import com.lynk.messageservice.infrastructure.outbound.persistence.cassandra.entity.RoomByMemberKey
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.data.cassandra.DataCassandraTest
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.context.annotation.Import
import org.springframework.data.cassandra.core.ReactiveCassandraTemplate
import org.testcontainers.cassandra.CassandraContainer
import org.testcontainers.cassandra.CassandraQueryWaitStrategy
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName
import reactor.test.StepVerifier
import java.util.*

@DataCassandraTest
@Import(RoomByMemberRepositoryImpl::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Testcontainers
class RoomByMemberRepositoryImplTest {

    @Autowired
    private lateinit var roomByMemberRepository: RoomByMemberRepositoryImpl

    @Autowired
    private lateinit var reactiveCassandraTemplate: ReactiveCassandraTemplate

    private val memberId = UUID.randomUUID()
    private val roomId = UUID.randomUUID()

    companion object {
        @Container
        @ServiceConnection
        val cassandraContainer: CassandraContainer =
            CassandraContainer(DockerImageName.parse("cassandra:5.0.6")).withExposedPorts(9042)
                .withInitScript("cassandra-init-data.cql").apply {
                    setWaitStrategy(CassandraQueryWaitStrategy())
                }
    }


    @AfterEach
    fun cleanup() {
        reactiveCassandraTemplate.truncate(RoomByMember::class.java).block()
    }

    @Test
    fun `createRoomByMember should insert a new record successfully`() {
        val result = roomByMemberRepository.createRoomByMember(
            memberId = memberId, roomId = roomId, name = "Test Room", avatarExtension = "png"
        )

        StepVerifier.create(result).expectNext(roomId).verifyComplete()
    }

    @Test
    fun `createRoomByMember should handle null avatarExtension`() {
        val result = roomByMemberRepository.createRoomByMember(
            memberId = memberId, roomId = roomId, name = "Another Room", avatarExtension = null
        )

        StepVerifier.create(result).expectNext(roomId).verifyComplete()
    }

    @Test
    fun `updateRoomByMember should update all provided fields`() {
        roomByMemberRepository.createRoomByMember(memberId, roomId, "Original Name").block()

        val updateResult = roomByMemberRepository.updateRoomByMember(
            memberId = memberId, roomId = roomId, name = "New Name"
        )

        StepVerifier.create(updateResult).expectNext(true).verifyComplete()

        val updatedRecord =
            reactiveCassandraTemplate.selectOneById(RoomByMemberKey(memberId, roomId), RoomByMember::class.java)

        StepVerifier.create(updatedRecord).expectNext(RoomByMember(RoomByMemberKey(memberId, roomId), "New Name"))
            .verifyComplete()
    }

    @Test
    fun `updateRoomByMember should update only specified fields`() {
        roomByMemberRepository.createRoomByMember(memberId, roomId, "Original Name").block()

        val updateResult = roomByMemberRepository.updateRoomByMember(
            memberId = memberId,
            roomId = roomId,
        )

        StepVerifier.create(updateResult).expectNext(true).verifyComplete()

        val updatedRecord = reactiveCassandraTemplate.selectOneById(
            RoomByMemberKey(memberId, roomId), RoomByMember::class.java
        )

        StepVerifier.create(updatedRecord)
            .expectNext(RoomByMember(RoomByMemberKey(memberId, roomId), name = "Original Name")).verifyComplete()
    }

    @Test
    fun `updateRoomByMember should complete successfully for non-existent record`() {
        val updateResult = roomByMemberRepository.updateRoomByMember(
            memberId = UUID.randomUUID(), roomId = UUID.randomUUID()
        )

        StepVerifier.create(updateResult).expectNext(true).verifyComplete()
    }
}
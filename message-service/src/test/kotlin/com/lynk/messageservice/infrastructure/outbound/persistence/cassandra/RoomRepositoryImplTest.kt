package com.lynk.messageservice.infrastructure.outbound.persistence.cassandra

import com.datastax.oss.driver.api.core.CqlSession
import com.lynk.messageservice.TestcontainersConfiguration
import com.lynk.messageservice.domain.model.RoomType
import com.lynk.messageservice.infrastructure.outbound.persistence.cassandra.entity.Room
import org.junit.jupiter.api.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.data.cassandra.ReactiveSession
import org.springframework.data.cassandra.core.ReactiveCassandraTemplate
import org.springframework.data.cassandra.core.cql.session.DefaultBridgedReactiveSession
import org.testcontainers.cassandra.CassandraContainer
import reactor.test.StepVerifier
import java.time.Instant
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Import(TestcontainersConfiguration::class)
class RoomRepositoryImplTest {

    @Autowired
    private lateinit var cassandraContainer: CassandraContainer
    private lateinit var reactiveCassandraTemplate: ReactiveCassandraTemplate
    private lateinit var repository: RoomRepositoryImpl
    private lateinit var session: ReactiveSession

    private val testCreatorId = UUID.randomUUID()

    @BeforeAll
    fun setUp() {
        val contactPoint = cassandraContainer.contactPoint
        val cqlSessionWithKeyspace =
            CqlSession.builder().addContactPoint(contactPoint).withLocalDatacenter("datacenter1")
                .withKeyspace("message_test_keyspace").build()

        session = DefaultBridgedReactiveSession(cqlSessionWithKeyspace)
        reactiveCassandraTemplate = ReactiveCassandraTemplate(session)
        repository = RoomRepositoryImpl(reactiveCassandraTemplate)
    }

    @AfterAll
    fun tearDown() {
        session.close()
    }

    @BeforeEach
    fun clearData() {
        session.execute("TRUNCATE room").block()
    }

    @Test
    fun `create should insert a room successfully`() {
        val room = Room(
            name = "Test Room",
            creatorId = testCreatorId,
            roomType = RoomType.PUBLIC.name,
            description = "A room for testing"
        )

        StepVerifier.create(repository.create(room))
            .assertNext { createdRoom ->
                assertNotNull(createdRoom.roomId)
                assertEquals(room.name, createdRoom.name)
                assertEquals(room.creatorId, createdRoom.creatorId)
                assertEquals(room.roomType, createdRoom.roomType)
                assertEquals(room.description, createdRoom.description)
            }
            .verifyComplete()
    }

    @Test
    fun `getById should retrieve an existing room`() {
        val room = Room(
            name = "Existing Room",
            creatorId = testCreatorId,
            roomType = RoomType.PRIVATE.name,
            avatarUrl = "http://example.com/avatar.png"
        )
        reactiveCassandraTemplate.insert(room).block()

        StepVerifier.create(repository.getById(room.roomId))
            .assertNext { foundRoom ->
                assertEquals(room.roomId, foundRoom.roomId)
                assertEquals(room.name, foundRoom.name)
                assertEquals(room.creatorId, foundRoom.creatorId)
                assertEquals(room.roomType, foundRoom.roomType)
                assertEquals(room.avatarUrl, foundRoom.avatarUrl)
            }
            .verifyComplete()
    }

    @Test
    fun `getById should return empty for non-existent room`() {
        StepVerifier.create(repository.getById(UUID.randomUUID()))
            .verifyComplete()
    }

    @Test
    fun `update should modify an existing room`() {
        val originalRoom = Room(
            name = "Original Name",
            creatorId = testCreatorId,
            roomType = RoomType.PUBLIC.name
        )
        reactiveCassandraTemplate.insert(originalRoom).block()

        val updatedRoom = originalRoom.copy(
            name = "Updated Name",
            roomType = RoomType.PRIVATE.name,
            avatarUrl = "http://new-avatar.png",
            lastActivityTimestamp = Instant.now().plusSeconds(3600),
            description = "Updated description"
        )

        StepVerifier.create(repository.update(updatedRoom))
            .assertNext { resultRoom ->
                assertEquals(updatedRoom.roomId, resultRoom.roomId)
                assertEquals(updatedRoom.name, resultRoom.name)
                assertEquals(updatedRoom.roomType, resultRoom.roomType)
                assertEquals(updatedRoom.avatarUrl, resultRoom.avatarUrl)
                assertEquals(updatedRoom.lastActivityTimestamp, resultRoom.lastActivityTimestamp)
                assertEquals(updatedRoom.description, resultRoom.description)
            }
            .verifyComplete()

        StepVerifier.create(repository.getById(originalRoom.roomId))
            .assertNext { foundRoom ->
                assertEquals("Updated Name", foundRoom.name)
                assertEquals(RoomType.PRIVATE.name, foundRoom.roomType)
            }
            .verifyComplete()
    }

    @Test
    fun `delete should remove a room successfully`() {
        val roomToDelete = Room(
            name = "Room to Delete",
            creatorId = testCreatorId,
            roomType = RoomType.PUBLIC.name
        )
        reactiveCassandraTemplate.insert(roomToDelete).block()

        StepVerifier.create(repository.authorizedDelete(roomToDelete.roomId,testCreatorId))
            .expectNext(true)
            .verifyComplete()

        StepVerifier.create(repository.getById(roomToDelete.roomId))
            .verifyComplete()
    }

    @Test
    fun `delete should return true for non-existent room`() {
        StepVerifier.create(repository.authorizedDelete(UUID.randomUUID(),testCreatorId))
            .expectNext(true)
            .verifyComplete()
    }
}
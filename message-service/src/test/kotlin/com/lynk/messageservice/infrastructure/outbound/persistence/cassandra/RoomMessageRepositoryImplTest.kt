package com.lynk.messageservice.infrastructure.outbound.persistence.cassandra

import com.lynk.messageservice.infrastructure.outbound.persistence.cassandra.entity.RoomMessage
import org.assertj.core.api.Assertions.assertThat
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
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.*

@DataCassandraTest
@Import(RoomMessageRepositoryImpl::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Testcontainers
class RoomMessageRepositoryImplTest {

    @Autowired
    private lateinit var roomMessageRepository: RoomMessageRepositoryImpl

    @Autowired
    private lateinit var reactiveCassandraTemplate: ReactiveCassandraTemplate

    private val roomId = UUID.randomUUID()
    private val senderId = UUID.randomUUID()

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
        reactiveCassandraTemplate.truncate(RoomMessage::class.java).block()
    }

    @Test
    fun `saveMessage should store a new message successfully`() {
        val result = roomMessageRepository.saveMessage(
            roomId = roomId, content = "Hello, Room!", senderId = senderId, messageId = UUID.randomUUID()
        )

        StepVerifier.create(result).expectNext(true).verifyComplete()
    }

    @Test
    fun `saveMessage should store a reply message with replyToMessageId`() {
        val originalMessageId = UUID.randomUUID()
        val replyMessageId = UUID.randomUUID()

        val result = roomMessageRepository.saveMessage(
            roomId = roomId,
            content = "This is a reply.",
            senderId = senderId,
            messageId = replyMessageId,
            replyToMessageId = originalMessageId
        )

        StepVerifier.create(result).expectNext(true).verifyComplete()
    }

    @Test
    fun `getMessagesByRoomId should retrieve messages within a time range`() {
        val now = Instant.now()
        roomMessageRepository.saveMessage(
            roomId,
            "Message 1",
            senderId,
            UUID.randomUUID(),
            timestamp = now.minusSeconds(10)
        ).block()
        roomMessageRepository.saveMessage(roomId, "Message 2", senderId, UUID.randomUUID(), timestamp = now).block()

        val messages = roomMessageRepository.getMessagesByRoomId(roomId, now.minusSeconds(20), now.plusSeconds(1))

        StepVerifier.create(messages.collectList()).assertNext {
                assertThat(it).hasSize(2)
            }.verifyComplete()
    }

    @Test
    fun `getMessagesByRoomId should retrieve messages across different time buckets`() {
        val now = Instant.now()
        val lastMonth = now.minus(35, ChronoUnit.DAYS)

        roomMessageRepository.saveMessage(roomId, "Recent Message", senderId, UUID.randomUUID(), timestamp = now)
            .block()
        roomMessageRepository.saveMessage(roomId, "Old Message", senderId, UUID.randomUUID(), timestamp = lastMonth)
            .block()

        val messages = roomMessageRepository.getMessagesByRoomId(roomId, lastMonth.minusSeconds(1), now.plusSeconds(1))

        StepVerifier.create(messages.collectList()).assertNext {
                assertThat(it).hasSize(2)
            }.verifyComplete()
    }

    @Test
    fun `getMessagesByRoomId should return empty flux for a non-existent room`() {
        val messages = roomMessageRepository.getMessagesByRoomId(UUID.randomUUID(), Instant.now(), Instant.now())

        StepVerifier.create(messages).expectNextCount(0).verifyComplete()
    }

    @Test
    fun `getMessagesByRoomId should return empty flux for a time range with no messages`() {
        val now = Instant.now()
        roomMessageRepository.saveMessage(roomId, "A message", senderId, UUID.randomUUID(), timestamp = now).block()

        val messages = roomMessageRepository.getMessagesByRoomId(roomId, now.plusSeconds(10), now.plusSeconds(20))

        StepVerifier.create(messages).expectNextCount(0).verifyComplete()
    }
}
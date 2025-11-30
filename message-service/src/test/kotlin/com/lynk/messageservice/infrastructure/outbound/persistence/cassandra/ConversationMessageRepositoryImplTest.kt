package com.lynk.messageservice.infrastructure.outbound.persistence.cassandra

import com.lynk.messageservice.infrastructure.outbound.persistence.cassandra.entity.ConversationMessageEntity
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
@Import(ConversationMessageRepositoryImpl::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Testcontainers
class ConversationMessageRepositoryImplTest {

    @Autowired
    private lateinit var messageRepository: ConversationMessageRepositoryImpl

    @Autowired
    private lateinit var reactiveCassandraTemplate: ReactiveCassandraTemplate

    private val user1 = UUID.randomUUID().toString()
    private val user2 = UUID.randomUUID().toString()

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
    fun truncateTable() {
        reactiveCassandraTemplate.truncate(ConversationMessageEntity::class.java).block()
    }

    @Test
    fun `store should save a message successfully`() {
        val timestamp = Instant.now()
        val result = messageRepository.store("Hello", user1, user2, timestamp)

        StepVerifier.create(result).expectNext(true).verifyComplete()
    }

    @Test
    fun `get should retrieve messages within a time range`() {
        val now = Instant.now()
        messageRepository.store("Message 1", user1, user2, now.minusSeconds(10)).block()
        messageRepository.store("Message 2", user2, user1, now).block()

        val messages = messageRepository.get(user1, user2, now.minusSeconds(20), now.plusSeconds(1))

        StepVerifier.create(messages).expectNextCount(2).verifyComplete()
    }

    @Test
    fun `delete should remove messages within a time range`() {
        val now = Instant.now()
        messageRepository.store("To be deleted", user1, user2, now).block()
        val result = messageRepository.delete(user1, user2, now.minusSeconds(1), now.plusSeconds(1))

        StepVerifier.create(result).expectNext(true).verifyComplete()

        // Verify that the message is actually deleted
        val messages = messageRepository.get(user1, user2, now.minusSeconds(1), now.plusSeconds(1))
        StepVerifier.create(messages).expectNextCount(0).verifyComplete()
    }

    @Test
    fun `get should return messages in descending order of timestamp`() {
        val now = Instant.now()
        val messageContent1 = "First Message"
        val messageContent2 = "Second Message"
        messageRepository.store(messageContent1, user1, user2, now.minusSeconds(10)).block()
        messageRepository.store(messageContent2, user2, user1, now).block()

        val messages = messageRepository.get(user1, user2, now.minusSeconds(20), now.plusSeconds(1))

        StepVerifier.create(messages).expectNextMatches { it.content == messageContent2 }
            .expectNextMatches { it.content == messageContent1 }.verifyComplete()
    }

    @Test
    fun `get should retrieve messages across multiple time buckets`() {
        val now = Instant.now()
        val lastMonth = now.minus(30, ChronoUnit.DAYS)
        messageRepository.store("Recent Message", user1, user2, now).block()
        messageRepository.store("Old Message", user1, user2, lastMonth).block()

        val messages = messageRepository.get(user1, user2, lastMonth.minusSeconds(1), now.plusSeconds(1))

        StepVerifier.create(messages).expectNextCount(2).verifyComplete()
    }

    @Test
    fun `get should handle user order permutation correctly`() {
        val now = Instant.now()
        messageRepository.store("A message", user1, user2, now).block()

        val messages = messageRepository.get(user2, user1, now.minusSeconds(1), now.plusSeconds(1))

        StepVerifier.create(messages).expectNextCount(1).verifyComplete()
    }

    // --- Edge Cases ---

    @Test
    fun `get should return empty flux for non-existent conversation`() {
        val messages = messageRepository.get(
            UUID.randomUUID().toString(), UUID.randomUUID().toString(), Instant.now(), Instant.now()
        )

        StepVerifier.create(messages).expectNextCount(0).verifyComplete()
    }

    @Test
    fun `get should return empty flux for time range with no messages`() {
        val now = Instant.now()
        messageRepository.store("A message", user1, user2, now).block()

        val messages = messageRepository.get(user1, user2, now.plusSeconds(10), now.plusSeconds(20))

        StepVerifier.create(messages).expectNextCount(0).verifyComplete()
    }

    @Test
    fun `store should handle empty message content`() {
        val result = messageRepository.store("", user1, user2, Instant.now())

        StepVerifier.create(result).expectNext(true).verifyComplete()
    }

    @Test
    fun `delete should complete successfully for non-existent conversation`() {
        val now = Instant.now()
        val result = messageRepository.delete(
            UUID.randomUUID().toString(), UUID.randomUUID().toString(), now, now.plusSeconds(1)
        )

        StepVerifier.create(result).expectNext(true).verifyComplete()
    }
}
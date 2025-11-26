package com.lynk.messageservice.infrastructure.outbound.persistence.cassandra

import com.lynk.messageservice.TestcontainersConfiguration
import com.lynk.messageservice.infrastructure.outbound.persistence.cassandra.entity.ConversationEntity
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.data.cassandra.core.ReactiveCassandraTemplate
import org.testcontainers.junit.jupiter.Testcontainers
import reactor.test.StepVerifier
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.UUID

@SpringBootTest
@Import(TestcontainersConfiguration::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Testcontainers
class ConversationRepositoryImplTest {

    @Autowired
    private lateinit var conversationRepository: ConversationRepositoryImpl

    @Autowired
    private lateinit var reactiveCassandraTemplate: ReactiveCassandraTemplate

    private val userId1 = UUID.randomUUID()
    private val userId2 = UUID.randomUUID()
    private val recipientId1 = UUID.randomUUID()
    private val recipientId2 = UUID.randomUUID()

    @AfterEach
    fun cleanup() {
        reactiveCassandraTemplate.truncate(ConversationEntity::class.java).block()
    }


    @Test
    fun `store should save a conversation successfully`() {
        val result = conversationRepository.store("Hello there!", userId1, recipientId1)

        StepVerifier.create(result)
            .expectNext(true)
            .verifyComplete()
    }

    @Test
    fun `store should handle empty message preview`() {
        val result = conversationRepository.store("", userId1, recipientId1)

        StepVerifier.create(result)
            .expectNext(true)
            .verifyComplete()

        // Verify that the empty string was stored
        StepVerifier.create(
            conversationRepository.get(userId1.toString(), Instant.now().minus(1, ChronoUnit.MINUTES), Instant.now().plus(1, ChronoUnit.MINUTES))
        )
            .verifyComplete()
    }

    // --- Get Method Tests ---

    @Test
    fun `get should retrieve conversations within a specific time range`() {
        val now = Instant.now()
        // This one should be retrieved
        conversationRepository.store("Recent message", userId1, recipientId1).block()
        // This one is too old
        val oldConversation = ConversationEntity(
            key = com.lynk.messageservice.infrastructure.outbound.persistence.cassandra.entity.ConversationKey(
                userId = userId1,
                recipientId = recipientId2
            ),
            lastActivityTimestamp = now.minus(2, ChronoUnit.HOURS),
        )
        reactiveCassandraTemplate.insert(oldConversation).block()

        val conversations = conversationRepository.get(userId1.toString(), now.minus(1, ChronoUnit.MINUTES), now.plus(1, ChronoUnit.MINUTES))

        StepVerifier.create(conversations)
            .expectNextCount(1)
            .verifyComplete()
    }

    @Test
    fun `get should return conversations in descending order of timestamp`() {
        val now = Instant.now()
        conversationRepository.store("Older Message", userId1, recipientId1).block()
        Thread.sleep(10) // ensure distinct timestamps
        conversationRepository.store("Newer Message", userId1, recipientId2).block()


        val conversations = conversationRepository.get(userId1.toString(), now.minus(1, ChronoUnit.MINUTES), now.plus(1, ChronoUnit.MINUTES))

        StepVerifier.create(conversations)
            .verifyComplete()
    }

    @Test
    fun `get should return empty flux for a user with no conversations`() {
        val conversations = conversationRepository.get(UUID.randomUUID().toString(), Instant.now().minus(1, ChronoUnit.HOURS), Instant.now())

        StepVerifier.create(conversations)
            .expectNextCount(0)
            .verifyComplete()
    }

    @Test
    fun `get should return empty flux for a valid user but empty time range`() {
        conversationRepository.store("A message", userId1, recipientId1).block()
        val conversations = conversationRepository.get(userId1.toString(), Instant.now().plus(1, ChronoUnit.HOURS), Instant.now().plus(2, ChronoUnit.HOURS))

        StepVerifier.create(conversations)
            .expectNextCount(0)
            .verifyComplete()
    }

    @Test
    fun `get should handle time range boundaries correctly`() {
        val boundaryTime = Instant.now()
        val conversationOnBoundary = ConversationEntity(
            key = com.lynk.messageservice.infrastructure.outbound.persistence.cassandra.entity.ConversationKey(
                userId = userId1,
                recipientId = recipientId1
            ),
            lastActivityTimestamp = boundaryTime,
        )
        reactiveCassandraTemplate.insert(conversationOnBoundary).block()

        // Should be included because 'start' is inclusive (gte)
        val conversations = conversationRepository.get(userId1.toString(), boundaryTime, boundaryTime.plus(1, ChronoUnit.SECONDS))

        StepVerifier.create(conversations)
            .expectNextCount(1)
            .verifyComplete()

        val conversations2 = conversationRepository.get(userId1.toString(), boundaryTime.minus(1, ChronoUnit.SECONDS), boundaryTime)

        StepVerifier.create(conversations2)
            .expectNextCount(0)
            .verifyComplete()
    }

    @Test
    fun `delete should remove conversations within a time range`() {
        val now = Instant.now()
        conversationRepository.store("To be deleted", userId1, recipientId1).block()
        val oldConversation = ConversationEntity(
            key = com.lynk.messageservice.infrastructure.outbound.persistence.cassandra.entity.ConversationKey(
                userId = userId1,
                recipientId = recipientId2
            ),
            lastActivityTimestamp = now.minus(2, ChronoUnit.HOURS),
        )
        reactiveCassandraTemplate.insert(oldConversation).block()

        val deleteResult = conversationRepository.delete(userId1.toString(), now.minus(1, ChronoUnit.MINUTES), now.plus(1, ChronoUnit.MINUTES))

        StepVerifier.create(deleteResult)
            .expectNext(true)
            .verifyComplete()

        val remainingConversations = conversationRepository.get(userId1.toString(), now.minus(3, ChronoUnit.HOURS), now.plus(1, ChronoUnit.MINUTES))
        StepVerifier.create(remainingConversations)
            .expectNextCount(1)
            .verifyComplete()
    }

    @Test
    fun `delete should only remove conversations for the specified user`() {
        val now = Instant.now()
        conversationRepository.store("User1's message", userId1, recipientId1).block()
        conversationRepository.store("User2's message", userId2, recipientId1).block()

        val deleteResult = conversationRepository.delete(userId1.toString(), now.minus(1, ChronoUnit.MINUTES), now.plus(1, ChronoUnit.MINUTES))

        StepVerifier.create(deleteResult)
            .expectNext(true)
            .verifyComplete()

        val user2Conversations = conversationRepository.get(userId2.toString(), now.minus(1, ChronoUnit.MINUTES), now.plus(1, ChronoUnit.MINUTES))
        StepVerifier.create(user2Conversations)
            .expectNextCount(1)
            .verifyComplete()
    }

    @Test
    fun `delete should complete successfully for a user with no conversations`() {
        val result = conversationRepository.delete(UUID.randomUUID().toString(), Instant.now().minus(1, ChronoUnit.HOURS), Instant.now())

        StepVerifier.create(result)
            .expectNext(true)
            .verifyComplete()
    }
}
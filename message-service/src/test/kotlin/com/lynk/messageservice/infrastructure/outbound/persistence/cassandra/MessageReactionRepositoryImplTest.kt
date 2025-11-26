package com.lynk.messageservice.infrastructure.outbound.persistence.cassandra

import com.lynk.messageservice.TestcontainersConfiguration
import com.lynk.messageservice.infrastructure.outbound.persistence.cassandra.entity.MessageReaction
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
class MessageReactionRepositoryImplTest {

    @Autowired
    private lateinit var messageReactionRepository: MessageReactionRepositoryImpl

    @Autowired
    private lateinit var reactiveCassandraTemplate: ReactiveCassandraTemplate

    private val roomId = UUID.randomUUID()
    private val messageId = UUID.randomUUID()
    private val memberId1 = UUID.randomUUID()
    private val memberId2 = UUID.randomUUID()

    @AfterEach
    fun cleanup() {
        reactiveCassandraTemplate.truncate(MessageReaction::class.java).block()
    }

    @Test
    fun `addReaction should save a new reaction successfully`() {
        val result = messageReactionRepository.addReaction(roomId, messageId, memberId1, "👍")

        StepVerifier.create(result)
            .expectNext(true)
            .verifyComplete()
    }

    @Test
    fun `addReaction should overwrite an existing reaction from the same member`() {
        messageReactionRepository.addReaction(roomId, messageId, memberId1, "👍").block()
        val overwriteResult = messageReactionRepository.addReaction(roomId, messageId, memberId1, "❤️")

        StepVerifier.create(overwriteResult)
            .expectNext(true)
            .verifyComplete()

        // Verify the emoji was updated
        StepVerifier.create(
            reactiveCassandraTemplate.selectOneById(
                com.lynk.messageservice.infrastructure.outbound.persistence.cassandra.entity.MessageReactionKey(roomId, messageId, memberId1),
                MessageReaction::class.java
            )
        )
            .expectNextMatches { it.emoji == "❤️" }
            .verifyComplete()
    }

    @Test
    fun `deleteReaction should remove an existing reaction`() {
        messageReactionRepository.addReaction(roomId, messageId, memberId1, "👍").block()

        val deleteResult = messageReactionRepository.deleteReaction(roomId, messageId, memberId1)

        StepVerifier.create(deleteResult)
            .expectNext(true)
            .verifyComplete()

        // Verify the reaction is gone
        StepVerifier.create(
            reactiveCassandraTemplate.selectOneById(
                com.lynk.messageservice.infrastructure.outbound.persistence.cassandra.entity.MessageReactionKey(roomId, messageId, memberId1),
                MessageReaction::class.java
            )
        )
            .verifyComplete()
    }

    @Test
    fun `deleteReaction should not affect other members' reactions`() {
        messageReactionRepository.addReaction(roomId, messageId, memberId1, "👍").block()
        messageReactionRepository.addReaction(roomId, messageId, memberId2, "😂").block()

        // Delete member 1's reaction
        messageReactionRepository.deleteReaction(roomId, messageId, memberId1).block()

        // Verify member 2's reaction still exists
        StepVerifier.create(
            reactiveCassandraTemplate.selectOneById(
                com.lynk.messageservice.infrastructure.outbound.persistence.cassandra.entity.MessageReactionKey(roomId, messageId, memberId2),
                MessageReaction::class.java
            )
        )
            .expectNextCount(1)
            .verifyComplete()
    }

    @Test
    fun `deleteReaction should complete successfully for a non-existent reaction`() {
        val result = messageReactionRepository.deleteReaction(roomId, messageId, memberId1)

        StepVerifier.create(result)
            .expectNext(true)
            .verifyComplete()
    }
}
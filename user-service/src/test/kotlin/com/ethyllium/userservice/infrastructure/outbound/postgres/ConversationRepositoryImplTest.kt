package com.ethyllium.userservice.infrastructure.outbound.postgres

import com.ethyllium.userservice.domain.model.Conversation
import com.ethyllium.userservice.infrastructure.outbound.postgres.entity.ConversationEntity
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
class ConversationRepositoryImplTest {

    @MockK
    lateinit var r2dbcEntityTemplate: R2dbcEntityTemplate

    @InjectMockKs
    lateinit var conversationRepository: ConversationRepositoryImpl

    private val conversationId = UUID.randomUUID()
    private val senderId = UUID.randomUUID()
    private val recipientId = UUID.randomUUID()
    private val isBlocked = false

    private val conversation = Conversation(conversationId, senderId, recipientId)
    private val conversationEntity = ConversationEntity(conversationId, senderId, recipientId)

    @Test
    fun `store inserts conversation entity and returns it`() {
        every { r2dbcEntityTemplate.insert(any<ConversationEntity>()) } returns Mono.just(conversationEntity)

        StepVerifier.create(conversationRepository.insert(conversation))
            .expectNext(conversationEntity.conversationId)
            .verifyComplete()

        verify {
            r2dbcEntityTemplate.insert(match<ConversationEntity> {
                it.conversationId == conversationId &&
                        it.senderId == senderId
            })
        }
    }

    @Test
    fun `delete removes entity and returns true on success`() {
        every {
            r2dbcEntityTemplate.delete(any<Query>(), ConversationEntity::class.java)
        } returns Mono.just(1)

        StepVerifier.create(conversationRepository.delete(conversationId))
            .expectNext(true)
            .verifyComplete()
    }

    @Test
    fun `delete returns false when no entity deleted`() {
        every {
            r2dbcEntityTemplate.delete(any<Query>(), ConversationEntity::class.java)
        } returns Mono.just(0)

        StepVerifier.create(conversationRepository.delete(conversationId))
            .expectNext(false)
            .verifyComplete()
    }

    @Test
    fun `select returns mapped Conversation when entity exists`() {
        every {
            r2dbcEntityTemplate.selectOne(any<Query>(), ConversationEntity::class.java)
        } returns Mono.just(conversationEntity)

        StepVerifier.create(conversationRepository.select(conversationId))
            .expectNext(conversation)
            .verifyComplete()
    }

    @Test
    fun `select completes empty when entity does not exist`() {
        every {
            r2dbcEntityTemplate.selectOne(any<Query>(), ConversationEntity::class.java)
        } returns Mono.empty()

        StepVerifier.create(conversationRepository.select(conversationId))
            .verifyComplete()
    }

    @Test
    fun `exists returns true when entity exists`() {
        every {
            r2dbcEntityTemplate.exists(any<Query>(), ConversationEntity::class.java)
        } returns Mono.just(true)

        StepVerifier.create(conversationRepository.exists(conversationId))
            .expectNext(true)
            .verifyComplete()
    }

    @Test
    fun `exists returns false when entity does not exist`() {
        every {
            r2dbcEntityTemplate.exists(any<Query>(), ConversationEntity::class.java)
        } returns Mono.just(false)

        StepVerifier.create(conversationRepository.exists(conversationId))
            .expectNext(false)
            .verifyComplete()
    }
}
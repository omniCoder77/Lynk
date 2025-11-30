package com.lynk.messageservice.infrastructure.outbound.persistence.cassandra

import com.lynk.messageservice.infrastructure.outbound.persistence.cassandra.entity.ConversationEntity
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
import java.util.*

@DataCassandraTest
@Import(ConversationRepositoryImpl::class)
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
        reactiveCassandraTemplate.truncate(ConversationEntity::class.java).block()
    }


    @Test
    fun `store should save a conversation successfully`() {
        val result = conversationRepository.store("Hello there!", userId1, recipientId1)

        StepVerifier.create(result).expectNext(true).verifyComplete()
    }

    @Test
    fun `get should return empty flux for a valid user but empty time range`() {
        conversationRepository.store("A message", userId1, recipientId1).block()
        val conversations = conversationRepository.get(userId1.toString(), userId2.toString())

        StepVerifier.create(conversations).expectNextCount(0).verifyComplete()
    }

    @Test
    fun `delete should only remove conversations for the specified user`() {
        Instant.now()
        conversationRepository.store("User1's message", userId1, recipientId1).block()
        conversationRepository.store("User2's message", userId2, recipientId2).block()

        val deleteResult = conversationRepository.delete(userId1, recipientId1)

        StepVerifier.create(deleteResult).expectNext(true).verifyComplete()

        val user2Conversations = conversationRepository.get(userId2.toString(), recipientId2.toString())
        StepVerifier.create(user2Conversations).expectNextCount(1).verifyComplete()
    }

    @Test
    fun `delete should complete successfully for a user with no conversations`() {
        val result = conversationRepository.delete(UUID.randomUUID(), userId1)

        StepVerifier.create(result).expectNext(true).verifyComplete()
    }
}
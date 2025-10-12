package com.lynk.messageservice.infrastructure.outbound.persistence.cassandra

import com.datastax.oss.driver.api.core.CqlSession
import com.lynk.messageservice.TestcontainersConfiguration
import com.lynk.messageservice.infrastructure.outbound.persistence.cassandra.entity.Conversation
import com.lynk.messageservice.infrastructure.outbound.persistence.cassandra.entity.ConversationKey
import org.junit.jupiter.api.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.data.cassandra.ReactiveSession
import org.springframework.data.cassandra.core.ReactiveCassandraTemplate
import org.springframework.data.cassandra.core.cql.session.DefaultBridgedReactiveSession
import org.springframework.data.cassandra.core.query.Query
import org.springframework.data.cassandra.core.query.where
import org.testcontainers.cassandra.CassandraContainer
import reactor.test.StepVerifier
import java.time.Instant
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Import(TestcontainersConfiguration::class)
class ConversationByUserRepositoryImplTest {

    @Autowired
    private lateinit var cassandraContainer: CassandraContainer
    private lateinit var reactiveCassandraTemplate: ReactiveCassandraTemplate
    private lateinit var repository: ConversationRepositoryImpl
    private lateinit var session: ReactiveSession

    private val testUserId = UUID.randomUUID()
    private val anotherUserId = UUID.randomUUID()

    @BeforeAll
    fun setUp() {
        val contactPoint = cassandraContainer.contactPoint
        println("Cassandra contact point: $contactPoint")

        val cqlSessionWithKeyspace =
            CqlSession.builder().addContactPoint(contactPoint).withLocalDatacenter("datacenter1")
                .withKeyspace("message_test_keyspace").build()

        session = DefaultBridgedReactiveSession(cqlSessionWithKeyspace)
        reactiveCassandraTemplate = ReactiveCassandraTemplate(session)
        repository = ConversationRepositoryImpl(reactiveCassandraTemplate)
    }

    @AfterAll
    fun tearDown() {
        session.close()
    }

    @BeforeEach
    fun clearData() {
        session.execute("TRUNCATE conversations_by_user").block()
    }

    private fun createAndInsertConversation(
        userId: UUID,
        timestamp: Instant,
        recipientId: String = UUID.randomUUID().toString(),
        message: String = "Test Message"
    ): Conversation {
        val conversation = Conversation(
            key = ConversationKey(userId = userId, lastActivityTimestamp = timestamp, recipientId = recipientId),
            conversationName = "Room",
            lastMessagePreview = message
        )
        return reactiveCassandraTemplate.insert(conversation).block()!!
    }

    @Test
    fun `store should insert conversation successfully`() {
        val recipientId = UUID.randomUUID().toString()

        val storeResult = repository.store("Hello", testUserId.toString(), recipientId)

        StepVerifier.create(storeResult).expectNext(true).verifyComplete()

        val query = Query.query(where("user_id").`is`(testUserId))
        StepVerifier.create(reactiveCassandraTemplate.selectOne(query, Conversation::class.java))
            .expectNextMatches {
                it.key.userId == testUserId &&
                        it.key.recipientId == recipientId &&
                        it.lastMessagePreview == "Hello" &&
                        it.conversationName == "Room"
            }.verifyComplete()
    }

    @Test
    fun `get should retrieve conversations within time range and be correctly ordered`() {
        val augustDate = Instant.parse("2025-08-15T10:00:00Z")
        val septemberDate = Instant.parse("2025-09-01T12:00:00Z")
        val earlyAugustDate = Instant.parse("2025-08-10T09:00:00Z")

        // These should be retrieved
        createAndInsertConversation(testUserId, augustDate, message = "Hello August")
        createAndInsertConversation(testUserId, septemberDate, message = "Hello September")
        createAndInsertConversation(testUserId, earlyAugustDate, message = "Hello Early August")

        // These should NOT be retrieved
        createAndInsertConversation(testUserId, Instant.parse("2025-07-10T10:00:00Z"), message = "Hello July")
        createAndInsertConversation(anotherUserId, septemberDate, message = "Other user's message")

        val startRange = Instant.parse("2025-08-01T00:00:00Z")
        val endRange = Instant.parse("2025-09-30T23:59:59Z")

        val result = repository.get(testUserId.toString(), startRange, endRange)

        StepVerifier.create(result.collectList()).assertNext { conversations ->
            assertEquals(3, conversations.size)

            // Verify content
            assertTrue(conversations.any { it.lastMessagePreview == "Hello September" })
            assertTrue(conversations.any { it.lastMessagePreview == "Hello August" })
            assertTrue(conversations.any { it.lastMessagePreview == "Hello Early August" })

            // Verify descending order by lastActivityTimestamp
            assertEquals("Hello September", conversations[0].lastMessagePreview)
            assertEquals("Hello August", conversations[1].lastMessagePreview)
            assertEquals("Hello Early August", conversations[2].lastMessagePreview)

        }.verifyComplete()
    }

    @Test
    fun `get should handle exact timestamp boundaries`() {
        val startTime = Instant.parse("2025-10-10T10:00:00Z")
        val endTime = Instant.parse("2025-10-10T12:00:00Z")

        // Included due to gte (greater than or equal)
        createAndInsertConversation(testUserId, startTime, message = "Start Time Match")
        // Excluded due to lt (less than)
        createAndInsertConversation(testUserId, endTime, message = "End Time Match - Excluded")
        // Included as it's within the range
        createAndInsertConversation(testUserId, startTime.plusSeconds(1), message = "Inside Range")

        val result = repository.get(testUserId.toString(), startTime, endTime)

        StepVerifier.create(result.collectList()).assertNext { conversations ->
            assertEquals(2, conversations.size)
            assertTrue(conversations.any { it.lastMessagePreview == "Start Time Match" })
            assertTrue(conversations.any { it.lastMessagePreview == "Inside Range" })
        }.verifyComplete()
    }


    @Test
    fun `get should return empty flux when no conversations found`() {
        val start = Instant.now().minusSeconds(3600)
        val end = Instant.now()

        val result = repository.get(UUID.randomUUID().toString(), start, end)

        StepVerifier.create(result).expectNextCount(0).verifyComplete()
    }

    @Test
    fun `delete should remove data within a time range for a specific user`() {
        createAndInsertConversation(testUserId, Instant.parse("2025-10-15T10:00:00Z"), message = "Msg Oct - To Delete")
        createAndInsertConversation(testUserId, Instant.parse("2025-11-15T10:00:00Z"), message = "Msg Nov - To Delete")
        createAndInsertConversation(testUserId, Instant.parse("2025-12-15T10:00:00Z"), message = "Msg Dec - Keep")
        createAndInsertConversation(
            anotherUserId,
            Instant.parse("2025-10-15T10:00:00Z"),
            message = "Other User Oct - Keep"
        )

        val startRange = Instant.parse("2025-10-01T00:00:00Z")
        val endRange = Instant.parse("2025-11-30T23:59:59Z")
        val deleteResult = repository.delete(testUserId.toString(), startRange, endRange)

        StepVerifier.create(deleteResult).expectNext(true).verifyComplete()

        // Verify that only the correct conversations were deleted for the target user
        val getResult = repository.get(
            testUserId.toString(),
            Instant.parse("2025-01-01T00:00:00Z"),
            Instant.parse("2025-12-31T23:59:59Z")
        )
        StepVerifier.create(getResult.collectList()).assertNext { remaining ->
            assertEquals(1, remaining.size)
            assertEquals("Msg Dec - Keep", remaining[0].lastMessagePreview)
        }.verifyComplete()

        // Verify the other user's data is untouched
        val getOtherUserResult = repository.get(
            anotherUserId.toString(),
            Instant.parse("2025-01-01T00:00:00Z"),
            Instant.parse("2025-12-31T23:59:59Z")
        )
        StepVerifier.create(getOtherUserResult.collectList()).assertNext { remaining ->
            assertEquals(1, remaining.size)
            assertEquals("Other User Oct - Keep", remaining[0].lastMessagePreview)
        }.verifyComplete()
    }

    @Test
    fun `delete should succeed for non-existent user`() {
        val nonExistentUserId = UUID.randomUUID().toString()
        val startRange = Instant.parse("2025-10-01T00:00:00Z")
        val endRange = Instant.parse("2025-10-31T23:59:59Z")

        val deleteResult = repository.delete(nonExistentUserId, startRange, endRange)

        StepVerifier.create(deleteResult)
            .expectNext(true)
            .verifyComplete()
    }
}
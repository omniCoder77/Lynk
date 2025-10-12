package com.lynk.messageservice.infrastructure.outbound.persistence.cassandra

import com.datastax.oss.driver.api.core.CqlSession
import com.lynk.messageservice.TestcontainersConfiguration
import com.lynk.messageservice.infrastructure.outbound.persistence.cassandra.entity.Message
import com.lynk.messageservice.infrastructure.util.UUIDUtils
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
class MessageRepositoryImplTest {

    @Autowired
    private lateinit var cassandraContainer: CassandraContainer
    private lateinit var reactiveCassandraTemplate: ReactiveCassandraTemplate
    private lateinit var repository: MessageRepositoryImpl
    private lateinit var session: ReactiveSession

    private val user1 = UUID.randomUUID().toString()
    private val user2 = UUID.randomUUID().toString()
    private val conversationId = UUIDUtils.getConversationId(user1, user2)

    @BeforeAll
    fun setUp() {
        val contactPoint = cassandraContainer.contactPoint
        val cqlSession = CqlSession.builder()
            .addContactPoint(contactPoint)
            .withLocalDatacenter("datacenter1")
            .withKeyspace("message_test_keyspace")
            .build()

        session = DefaultBridgedReactiveSession(cqlSession)
        reactiveCassandraTemplate = ReactiveCassandraTemplate(session)
        repository = MessageRepositoryImpl(reactiveCassandraTemplate)
    }

    @AfterAll
    fun tearDown() {
        session.close()
    }

    @BeforeEach
    fun clearData() {
        session.execute("TRUNCATE message").block()
    }

    @Test
    fun `store should insert message into correct bucket`() {
        val now = Instant.parse("2023-10-15T10:00:00Z")
        val expectedBucket = 202310
        val content = "Hello World"

        val result = repository.store(content, user1, user2, now)

        StepVerifier.create(result)
            .expectNext(true)
            .verifyComplete()

        val query = Query.query(
            where("conversation_id").`is`(conversationId),
            where("bucket").`is`(expectedBucket),
            where("message_timestamp").`is`(now)
        )

        StepVerifier.create(reactiveCassandraTemplate.selectOne(query, Message::class.java))
            .assertNext { msg ->
                assertEquals(conversationId, msg.key.conversationId)
                assertEquals(expectedBucket, msg.key.bucket)
                assertEquals(now, msg.key.messageTimestamp)
                assertEquals(UUID.fromString(user1), msg.senderId)
                assertEquals(content, msg.content)
            }
            .verifyComplete()
    }

    @Test
    fun `get should retrieve messages from a single bucket ordered descending`() {
        val baseTime = Instant.parse("2023-10-15T10:00:00Z")

        repository.store("Msg 1", user1, user2, baseTime).block()
        repository.store("Msg 2", user2, user1, baseTime.plusSeconds(60)).block()
        repository.store("Msg 3", user1, user2, baseTime.plusSeconds(120)).block()

        val start = baseTime.minusSeconds(1)
        val end = baseTime.plusSeconds(200)

        val result = repository.get(user1, user2, start, end)

        StepVerifier.create(result)
            .assertNext { assertEquals("Msg 3", it.content) } // Newest first
            .assertNext { assertEquals("Msg 2", it.content) }
            .assertNext { assertEquals("Msg 1", it.content) }
            .verifyComplete()
    }

    @Test
    fun `get should retrieve messages across multiple buckets`() {
        val timeOct = Instant.parse("2023-10-31T23:59:00Z")
        val timeNov = Instant.parse("2023-11-01T00:01:00Z")

        repository.store("October Msg", user1, user2, timeOct).block()
        repository.store("November Msg", user2, user1, timeNov).block()

        val start = Instant.parse("2023-10-01T00:00:00Z")
        val end = Instant.parse("2023-11-30T23:59:59Z")

        val result = repository.get(user1, user2, start, end)

        StepVerifier.create(result.collectList())
            .assertNext { messages ->
                assertEquals(2, messages.size)
                assertTrue(messages.any { it.content == "October Msg" && it.key.bucket == 202310 })
                assertTrue(messages.any { it.content == "November Msg" && it.key.bucket == 202311 })
            }
            .verifyComplete()
    }

    @Test
    fun `get should respect time range boundaries`() {
        val t1 = Instant.parse("2023-10-10T10:00:00Z")
        val t2 = Instant.parse("2023-10-10T11:00:00Z")
        val t3 = Instant.parse("2023-10-10T12:00:00Z")

        repository.store("Msg 1", user1, user2, t1).block()
        repository.store("Msg 2", user1, user2, t2).block()
        repository.store("Msg 3", user1, user2, t3).block()

        val result = repository.get(user1, user2, t1, t3)

        StepVerifier.create(result)
            .assertNext { assertEquals("Msg 2", it.content) } // DESC order
            .assertNext { assertEquals("Msg 1", it.content) }
            .verifyComplete()
    }

    @Test
    fun `delete should remove messages across buckets within time range`() {
        val timeOct = Instant.parse("2023-10-15T12:00:00Z")
        val timeNov = Instant.parse("2023-11-15T12:00:00Z")
        val timeDec = Instant.parse("2023-12-15T12:00:00Z")

        repository.store("To Delete Oct", user1, user2, timeOct).block()
        repository.store("To Delete Nov", user1, user2, timeNov).block()
        repository.store("To Keep Dec", user1, user2, timeDec).block()

        val startDelete = Instant.parse("2023-10-01T00:00:00Z")
        val endDelete = Instant.parse("2023-11-30T23:59:59Z")

        val deleteResult = repository.delete(user1, user2, startDelete, endDelete)

        StepVerifier.create(deleteResult)
            .expectNext(true)
            .verifyComplete()

        val getResult = repository.get(
            user1, user2,
            Instant.parse("2023-01-01T00:00:00Z"),
            Instant.parse("2024-01-01T00:00:00Z")
        )

        StepVerifier.create(getResult)
            .assertNext { assertEquals("To Keep Dec", it.content) }
            .verifyComplete()
    }

    @Test
    fun `repository should generate same conversationId regardless of sender or recipient order`() {
        val time = Instant.now()

        repository.store("Msg A", user1, user2, time).block()
        repository.store("Msg B", user2, user1, time.plusMillis(1)).block()

        val res1 = repository.get(user1, user2, time.minusSeconds(10), time.plusSeconds(10)).collectList().block()!!

        val res2 = repository.get(user2, user1, time.minusSeconds(10), time.plusSeconds(10)).collectList().block()!!

        assertEquals(2, res1.size)
        assertEquals(res1.size, res2.size)
        assertEquals(res1[0].key.conversationId, res2[0].key.conversationId)
    }
}
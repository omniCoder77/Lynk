package com.lynk.authservice.infrastructure.outbound.persistence.postgres

import com.lynk.authservice.domain.payload.entity.User
import com.lynk.authservice.infrastructure.outbound.persistence.postgres.entity.UserEntity
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.data.relational.core.query.Criteria
import org.springframework.data.relational.core.query.Query
import org.springframework.r2dbc.core.DatabaseClient
import org.testcontainers.containers.wait.strategy.Wait
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.postgresql.PostgreSQLContainer
import org.testcontainers.utility.DockerImageName
import reactor.test.StepVerifier
import java.util.*

@SpringBootTest
@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class UserRepositoryImplTest {

    @Autowired
    private lateinit var userRepository: UserRepositoryImpl

    @Autowired
    private lateinit var r2dbcEntityTemplate: R2dbcEntityTemplate

    @Autowired
    private lateinit var databaseClient: DatabaseClient

    companion object {
        @Container
        @ServiceConnection
        val postgresContainer = PostgreSQLContainer(DockerImageName.parse("postgres:18-alpine")).apply {
            withDatabaseName("auth_db-test")
            withUsername("postgres-test")
            withPassword("postgres-test")
        }.withExposedPorts(5432).waitingFor(Wait.forListeningPort())
    }

    @BeforeAll
    fun setup() {
        databaseClient.sql(
            """
            CREATE TABLE users (
                user_id UUID PRIMARY KEY,
                totp_secret VARCHAR(255),
                phone_number VARCHAR(32) NOT NULL,
                first_name VARCHAR(64) NOT NULL,
                last_name VARCHAR(64) NOT NULL,
                created_date TIMESTAMP NOT NULL,
                last_modified_date TIMESTAMP NOT NULL,
                enabled BOOLEAN DEFAULT FALSE,
                is_account_locked BOOLEAN DEFAULT FALSE,
                role VARCHAR(128) DEFAULT 'USER'
            );
        """.trimIndent()
        ).fetch().rowsUpdated().block()
    }

    @AfterAll
    fun cleanup() {
        databaseClient.sql("TRUNCATE TABLE users").fetch().rowsUpdated().block()
    }

    @Test
    fun `persist should save user and return userId`() {
        val userId = UUID.randomUUID()
        val user = User(
            userId.toString(), "testuser", phoneNumber = "1234567890", firstName = "testuser", lastName = "testuser"
        )

        val result = userRepository.persist(user)

        StepVerifier.create(result).expectNext(userId).verifyComplete()

        StepVerifier.create(
            r2dbcEntityTemplate.selectOne(
                Query.query(Criteria.where("user_id").`is`(userId)), UserEntity::class.java
            )
        ).expectNextMatches { it.userId == userId }.verifyComplete()
    }

    @Test
    fun `setTotpSecret should update totp secret and return affected rows`() {
        val userId = UUID.randomUUID()
        val initialUser = User(
            userId.toString(), "testuser", phoneNumber = "1234567890", firstName = "testuser", lastName = "testuser"
        )
        val totpSecret = "secret123"

        userRepository.persist(initialUser).block()

        val result = userRepository.setTotpSecret(userId, totpSecret)

        StepVerifier.create(result).expectNext(1L).verifyComplete()

        StepVerifier.create(
            r2dbcEntityTemplate.selectOne(
                Query.query(Criteria.where("user_id").`is`(userId)), UserEntity::class.java
            )
        ).expectNextMatches { it.totpSecret == totpSecret }.verifyComplete()
    }

    @Test
    fun `setTotpSecret should return zero when user not found`() {
        val nonExistentUserId = UUID.randomUUID()
        val totpSecret = "secret123"

        val result = userRepository.setTotpSecret(nonExistentUserId, totpSecret)

        StepVerifier.create(result).expectNext(0L).verifyComplete()
    }

    @Test
    fun `delete should remove user and return affected rows`() {
        val userId = UUID.randomUUID()
        val user = User(
            userId.toString(), "testuser", phoneNumber = "1234567890", firstName = "testuser", lastName = "testuser"
        )

        userRepository.persist(user).block()

        val result = userRepository.delete(userId)

        StepVerifier.create(result).expectNext(1L).verifyComplete()

        StepVerifier.create(
            r2dbcEntityTemplate.selectOne(
                Query.query(Criteria.where("user_id").`is`(userId)), UserEntity::class.java
            )
        ).verifyComplete()
    }

    @Test
    fun `delete should return zero when user not found`() {
        val nonExistentUserId = UUID.randomUUID()

        val result = userRepository.delete(nonExistentUserId)

        StepVerifier.create(result).expectNext(0L).verifyComplete()
    }
}
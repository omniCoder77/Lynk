package com.ethyllium.userservice.infrastructure.outbound.postgres

import com.ethyllium.userservice.domain.model.User
import com.ethyllium.userservice.infrastructure.outbound.postgres.entity.UserEntity
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.data.relational.core.query.Query
import org.springframework.data.relational.core.query.Update
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import java.util.*

@ExtendWith(MockKExtension::class)
class UserRepositoryImplTest {

    @MockK
    lateinit var r2dbcEntityTemplate: R2dbcEntityTemplate

    @InjectMockKs
    lateinit var userRepository: UserRepositoryImpl

    private val userId = UUID.randomUUID()
    private val username = "testuser"
    private val phoneNumber = "+1234567890"

    private val user = User(userId, username, phoneNumber)
    private val userEntity = UserEntity(userId, username, phoneNumber)

    @Test
    fun `insert stores user entity and returns userId`() {
        every { r2dbcEntityTemplate.insert(any<UserEntity>()) } returns Mono.just(userEntity)

        StepVerifier.create(userRepository.insert(user)).expectNext(userId).verifyComplete()
    }

    @Test
    fun `updateUsername updates specific field and returns true on success`() {
        val newUsername = "updatedUsername"
        every {
            r2dbcEntityTemplate.update(any<Query>(), any<Update>(), UserEntity::class.java)
        } returns Mono.just(1)

        StepVerifier.create(userRepository.updateUsername(userId, newUsername)).expectNext(true).verifyComplete()

        verify {
            r2dbcEntityTemplate.update(any<Query>(), any<Update>(), UserEntity::class.java)
        }
    }

    @Test
    fun `updateUsername returns false when no rows are updated`() {
        val newUsername = "updatedUsername"
        every {
            r2dbcEntityTemplate.update(any<Query>(), any<Update>(), UserEntity::class.java)
        } returns Mono.just(0)

        StepVerifier.create(userRepository.updateUsername(userId, newUsername)).expectNext(false).verifyComplete()
    }

    @Test
    fun `find returns mapped User when entity exists`() {
        every {
            r2dbcEntityTemplate.selectOne(any<Query>(), UserEntity::class.java)
        } returns Mono.just(userEntity)

        StepVerifier.create(userRepository.find(userId))
            .expectNext(user)
            .verifyComplete()
    }

    @Test
    fun `find completes empty when entity does not exist`() {
        every {
            r2dbcEntityTemplate.selectOne(any<Query>(), UserEntity::class.java)
        } returns Mono.empty()

        StepVerifier.create(userRepository.find(userId)).verifyComplete()
    }

    @Test
    fun `delete removes entity and returns true on success`() {
        every {
            r2dbcEntityTemplate.delete(any<Query>(), UserEntity::class.java)
        } returns Mono.just(1)

        StepVerifier.create(userRepository.delete(userId)).expectNext(true).verifyComplete()

        verify {
            r2dbcEntityTemplate.delete(any<Query>(), UserEntity::class.java)
        }
    }

    @Test
    fun `delete returns false when no entity deleted`() {
        every {
            r2dbcEntityTemplate.delete(any<Query>(), UserEntity::class.java)
        } returns Mono.just(0)

        // WHEN & THEN
        StepVerifier.create(userRepository.delete(userId)).expectNext(false).verifyComplete()
    }
}
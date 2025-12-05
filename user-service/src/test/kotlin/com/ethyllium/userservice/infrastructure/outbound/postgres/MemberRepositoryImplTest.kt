package com.ethyllium.userservice.infrastructure.outbound.postgres

import com.ethyllium.userservice.domain.model.Member
import com.ethyllium.userservice.domain.model.MemberRole
import com.ethyllium.userservice.infrastructure.outbound.postgres.entity.MemberEntity
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
import java.time.Instant
import java.util.UUID

@ExtendWith(MockKExtension::class)
class MemberRepositoryImplTest {

    @MockK
    lateinit var r2dbcEntityTemplate: R2dbcEntityTemplate

    @InjectMockKs
    lateinit var memberRepository: MemberRepositoryImpl

    private val memberId = UUID.randomUUID()
    private val joinedAt = Instant.now()
    private val role = MemberRole.MEMBER
    private val isAllowedToMessage = true
    private val isAllowedToSendMedia = true

    private val member = Member(memberId, joinedAt, role, isAllowedToMessage, isAllowedToSendMedia)
    private val memberEntity = MemberEntity(memberId, joinedAt, role, isAllowedToMessage, isAllowedToSendMedia)

    @Test
    fun `get returns mapped Member when entity exists`() {
        every {
            r2dbcEntityTemplate.selectOne(any<Query>(), MemberEntity::class.java)
        } returns Mono.just(memberEntity)

        StepVerifier.create(memberRepository.get(memberId))
            .expectNext(member)
            .verifyComplete()

        verify {
            r2dbcEntityTemplate.selectOne(any<Query>(), MemberEntity::class.java)
        }
    }

    @Test
    fun `get completes empty when entity does not exist`() {
        every {
            r2dbcEntityTemplate.selectOne(any<Query>(), MemberEntity::class.java)
        } returns Mono.empty()

        StepVerifier.create(memberRepository.get(memberId))
            .verifyComplete()
    }

    @Test
    fun `store inserts member entity and returns memberId`() {
        every { r2dbcEntityTemplate.insert(any<MemberEntity>()) } returns Mono.just(memberEntity)

        StepVerifier.create(memberRepository.store(member))
            .expectNext(memberId)
            .verifyComplete()

        verify {
            r2dbcEntityTemplate.insert(match<MemberEntity> {
                it.memberId == memberId &&
                        it.role == role
            })
        }
    }

    @Test
    fun `delete removes entity and returns true on success`() {
        every {
            r2dbcEntityTemplate.delete(any<Query>(), MemberEntity::class.java)
        } returns Mono.just(1)

        StepVerifier.create(memberRepository.delete(memberId))
            .expectNext(true)
            .verifyComplete()
    }

    @Test
    fun `delete returns false when no entity deleted`() {
        every {
            r2dbcEntityTemplate.delete(any<Query>(), MemberEntity::class.java)
        } returns Mono.just(0)

        StepVerifier.create(memberRepository.delete(memberId))
            .expectNext(false)
            .verifyComplete()
    }

    @Test
    fun `update performs update when fields are provided`() {
        every {
            r2dbcEntityTemplate.update(any<Query>(), any<Update>(), MemberEntity::class.java)
        } returns Mono.just(1)

        StepVerifier.create(
            memberRepository.update(
                role = MemberRole.ADMIN,
                memberId = memberId,
                isAllowedToMessage = false
            )
        )
            .expectNext(true)
            .verifyComplete()

        verify {
            r2dbcEntityTemplate.update(any<Query>(), any<Update>(), MemberEntity::class.java)
        }
    }

    @Test
    fun `update returns false instantly when no fields provided`() {
        StepVerifier.create(memberRepository.update(memberId = memberId))
            .expectNext(false)
            .verifyComplete()

        verify(exactly = 0) {
            r2dbcEntityTemplate.update(any<Query>(), any<Update>(), MemberEntity::class.java)
        }
    }
}
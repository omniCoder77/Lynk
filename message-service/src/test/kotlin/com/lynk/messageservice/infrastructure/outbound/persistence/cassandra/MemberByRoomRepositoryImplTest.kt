package com.lynk.messageservice.infrastructure.outbound.persistence.cassandra

import com.lynk.messageservice.domain.model.RoomRole
import com.lynk.messageservice.domain.port.driver.MemberByRoomRepository
import com.lynk.messageservice.infrastructure.outbound.persistence.cassandra.entity.MemberByRoom
import org.junit.jupiter.api.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.cache.CacheManager
import org.springframework.data.cassandra.core.ReactiveCassandraTemplate
import reactor.test.StepVerifier
import java.util.*

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class MemberByRoomRepositoryImplTest {

    @Autowired
    private lateinit var memberByRoomRepository: MemberByRoomRepository

    @Autowired
    private lateinit var reactiveCassandraTemplate: ReactiveCassandraTemplate

    @Autowired
    private lateinit var cacheManager: CacheManager

    private val roomId1 = UUID.randomUUID()
    private val memberId1 = UUID.randomUUID()
    private val memberId2 = UUID.randomUUID()
    private val memberId3 = UUID.randomUUID()
    private val memberId4 = UUID.randomUUID()

    @BeforeEach
    fun setup() {
        cacheManager.getCache("membersByRoom")?.clear()
        cacheManager.getCache("memberByRoomAndId")?.clear()
        reactiveCassandraTemplate.truncate(MemberByRoom::class.java).block()
    }

    @Nested
    @DisplayName("createMemberByRoom")
    inner class CreateMemberByRoomTests {

        @Test
        @DisplayName("should successfully create a new member and evict caches")
        fun `should successfully create a new member and evict caches`() {
            cacheManager.getCache("membersByRoom")?.put(roomId1, "stale_data")
            Assertions.assertNotNull(cacheManager.getCache("membersByRoom")?.get(roomId1))

            val result = memberByRoomRepository.createMemberByRoom(
                roomId1, memberId1, RoomRole.MEMBER, "Test User 1", "Description 1"
            )
            StepVerifier.create(result).expectNext(true).verifyComplete()

            StepVerifier.create(memberByRoomRepository.getMemberById(memberId1, roomId1)).expectNextCount(1)
                .verifyComplete()

            Assertions.assertNull(cacheManager.getCache("membersByRoom")?.get(roomId1))
        }
    }

    @Nested
    @DisplayName("updateRoom")
    inner class UpdateRoomTests {

        private val roomId2 = UUID.randomUUID()

        @BeforeEach
        fun setupUpdateRoom() {
            memberByRoomRepository.createMemberByRoom(roomId2, memberId3, RoomRole.ADMIN, "Admin User").block()
            memberByRoomRepository.createMemberByRoom(roomId2, memberId1, RoomRole.MEMBER, "Member 1").block()
            memberByRoomRepository.createMemberByRoom(roomId2, memberId2, RoomRole.MEMBER, "Member 2").block()
            memberByRoomRepository.createMemberByRoom(roomId2, memberId4, RoomRole.MEMBER, "Non-Admin Updater").block()
        }

        @Test
        @DisplayName("should update all members in a room if updater is an admin")
        fun `should update all members in a room if updater is an admin`() {
            val result = memberByRoomRepository.updateRoom(
                "New Desc", "new_avatar.png", "New Name", roomId2, memberId3 // Admin updater
            )

            StepVerifier.create(result.collectList()).assertNext { updatedIds ->
                Assertions.assertEquals(4, updatedIds.size)
                Assertions.assertTrue(updatedIds.containsAll(listOf(memberId1, memberId2, memberId3, memberId4)))
            }.verifyComplete()
        }

        @Test
        @DisplayName("should return empty flux if updater is not an admin")
        fun `should return empty flux if updater is not an admin`() {
            val result = memberByRoomRepository.updateRoom(
                "Attempted Update", null, null, roomId2, memberId4
            )
            StepVerifier.create(result).verifyComplete()
        }
    }

    @Nested
    @DisplayName("getMembersByRoomId")
    inner class GetMembersByRoomIdTests {
        @Test
        @DisplayName("should retrieve members from cache on subsequent calls")
        fun `should retrieve members from cache on subsequent calls`() {
            memberByRoomRepository.createMemberByRoom(roomId1, memberId1, RoomRole.MEMBER, "Cached User").block()

            memberByRoomRepository.getMembersByRoomId(roomId1).collectList().block()
            Assertions.assertNotNull(cacheManager.getCache("membersByRoom")?.get(roomId1))

            reactiveCassandraTemplate.truncate(MemberByRoom::class.java).block()

            val result = memberByRoomRepository.getMembersByRoomId(roomId1)
            StepVerifier.create(result.collectList()).expectNextMatches { members ->
                members.size == 1 && members.any { it.memberId == memberId1 }
            }.verifyComplete()
        }
    }

    @Nested
    @DisplayName("getMemberById")
    inner class GetMemberByIdTests {
        @Test
        @DisplayName("should retrieve a specific member by room ID and member ID")
        fun `should retrieve a specific member by room ID and member ID`() {
            memberByRoomRepository.createMemberByRoom(roomId1, memberId1, RoomRole.MEMBER, "Specific Member").block()
            val result = memberByRoomRepository.getMemberById(memberId1, roomId1)
            StepVerifier.create(result).expectNextMatches { it.display_name == "Specific Member" }.verifyComplete()

            val cachedMember =
                cacheManager.getCache("memberByRoomAndId")?.get("$roomId1:$memberId1", MemberByRoom::class.java)
            Assertions.assertNotNull(cachedMember)
            Assertions.assertEquals("Specific Member", cachedMember?.display_name)
        }
    }
}
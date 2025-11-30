package com.lynk.messageservice.infrastructure.outbound.persistence.cassandra

import com.lynk.messageservice.domain.model.RoomRole
import com.lynk.messageservice.domain.port.driver.MemberByRoomRepository
import com.lynk.messageservice.infrastructure.outbound.persistence.cassandra.entity.MemberByRoom
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
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
import reactor.core.publisher.Flux
import reactor.test.StepVerifier
import java.util.*

@DataCassandraTest
@Import(MemberByRoomRepositoryImpl::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Testcontainers
class MemberByRoomRepositoryImplTest {

    @Autowired
    private lateinit var memberByRoomRepository: MemberByRoomRepository

    @Autowired
    private lateinit var reactiveCassandraTemplate: ReactiveCassandraTemplate

    private val roomId1 = UUID.randomUUID()
    private val memberId1 = UUID.randomUUID()
    private val memberId2 = UUID.randomUUID()

    companion object {
        @Container
        @ServiceConnection
        val cassandraContainer: CassandraContainer =
            CassandraContainer(DockerImageName.parse("cassandra:5.0.6")).withExposedPorts(9042)
                .withInitScript("cassandra-init-data.cql").apply {
                    setWaitStrategy(CassandraQueryWaitStrategy())
                }
    }

    @BeforeEach
    fun setup() {
        reactiveCassandraTemplate.truncate(MemberByRoom::class.java).block()
    }

    @Test
    fun `createMemberByRoom - should successfully insert a new member`() {
        val displayName = "John Doe"
        val description = "A new member"

        val result = memberByRoomRepository.createMemberByRoom(
            roomId1, memberId1, RoomRole.MEMBER, displayName, description
        )

        StepVerifier.create(result).expectNext(true).verifyComplete()

        val storedMember = memberByRoomRepository.getMemberById(memberId1, roomId1)
        StepVerifier.create(storedMember).assertNext { member ->
                assertEquals(roomId1, member.memberByRoomKey.roomId)
                assertEquals(memberId1, member.memberByRoomKey.memberId)
                assertEquals(displayName, member.display_name)
                assertEquals(RoomRole.MEMBER.name, member.role)
                assertNotNull(member.joined_at)
            }.verifyComplete()
    }

    @Test
    fun `getMembersByRoomId - should return all members for a specific room`() {
        val randomRoom = UUID.randomUUID()

        val insert1 = memberByRoomRepository.createMemberByRoom(roomId1, memberId1, RoomRole.ADMIN, "Admin", "Desc")
        val insert2 = memberByRoomRepository.createMemberByRoom(roomId1, memberId2, RoomRole.MEMBER, "User", "Desc")
        val insert3 =
            memberByRoomRepository.createMemberByRoom(randomRoom, UUID.randomUUID(), RoomRole.MEMBER, "Other", "Desc")

        Flux.merge(insert1, insert2, insert3).blockLast()

        val result = memberByRoomRepository.getMembersByRoomId(roomId1)

        StepVerifier.create(result).expectNextCount(2).verifyComplete()
    }

    @Test
    fun `deleteMemberByRoomId - should remove member and return true`() {
        memberByRoomRepository.createMemberByRoom(roomId1, memberId1, RoomRole.MEMBER, "To Delete", null).block()

        val deleteResult = memberByRoomRepository.deleteMemberByRoomId(roomId1, memberId1)

        StepVerifier.create(deleteResult).expectNext(true).verifyComplete()

        StepVerifier.create(memberByRoomRepository.getMemberById(memberId1, roomId1)).verifyComplete()
    }

    @Test
    fun `updateRoom - should update all members in room when updater is ADMIN`() {
        memberByRoomRepository.createMemberByRoom(roomId1, memberId1, RoomRole.ADMIN, "OldName1", "OldDesc1").block()
        memberByRoomRepository.createMemberByRoom(roomId1, memberId2, RoomRole.MEMBER, "OldName2", "OldDesc2").block()

        val newDesc = "Updated Room Description"
        val newDisplayName = "Updated Room Name"

        val updateFlux = memberByRoomRepository.updateRoom(
            description = newDesc,
            avatarUrl = "ignored_by_impl",
            displayName = newDisplayName,
            roomId = roomId1,
            updater = memberId1
        )

        StepVerifier.create(updateFlux).expectNextCount(2).verifyComplete()

        StepVerifier.create(memberByRoomRepository.getMemberById(memberId2, roomId1)).assertNext { member ->
                assertEquals(newDesc, member.description)
                assertEquals(newDisplayName, member.display_name)
                assertEquals(RoomRole.MEMBER.name, member.role)
            }.verifyComplete()
    }

    @Test
    fun `updateRoom - should NOT update anything if updater is NOT ADMIN`() {
        memberByRoomRepository.createMemberByRoom(roomId1, memberId1, RoomRole.MEMBER, "User1", "Desc1").block()
        memberByRoomRepository.createMemberByRoom(roomId1, memberId2, RoomRole.MEMBER, "User2", "Desc2").block()

        val updateFlux = memberByRoomRepository.updateRoom(
            description = "Hacker Update",
            avatarUrl = null,
            displayName = "Hacked Name",
            roomId = roomId1,
            updater = memberId1
        )

        StepVerifier.create(updateFlux).expectNextCount(0).verifyComplete()

        StepVerifier.create(memberByRoomRepository.getMemberById(memberId2, roomId1)).assertNext { member ->
                assertEquals("Desc2", member.description)
            }.verifyComplete()
    }

    @Test
    fun `updateRoom - should handle partial updates (null values)`() {
        memberByRoomRepository.createMemberByRoom(roomId1, memberId1, RoomRole.ADMIN, "OriginalName", "OriginalDesc")
            .block()

        val updateFlux = memberByRoomRepository.updateRoom(
            description = null, avatarUrl = null, displayName = "NewName", roomId = roomId1, updater = memberId1
        )

        StepVerifier.create(updateFlux).expectNextCount(1).verifyComplete()

        StepVerifier.create(memberByRoomRepository.getMemberById(memberId1, roomId1)).assertNext { member ->
                assertEquals("NewName", member.display_name)
                assertEquals("OriginalDesc", member.description)
            }.verifyComplete()
    }

    @Test
    fun `getMemberById - should return empty for non-existent member`() {
        StepVerifier.create(memberByRoomRepository.getMemberById(UUID.randomUUID(), roomId1)).verifyComplete()
    }

    @Test
    fun `getMembersByRoomId - should return empty Flux for non-existent room`() {
        StepVerifier.create(memberByRoomRepository.getMembersByRoomId(UUID.randomUUID())).expectNextCount(0)
            .verifyComplete()
    }

    @Test
    fun `createMemberByRoom - duplicate insertion should overwrite (Upsert behavior)`() {
        memberByRoomRepository.createMemberByRoom(roomId1, memberId1, RoomRole.MEMBER, "Initial Name", "Initial Desc")
            .block()

        memberByRoomRepository.createMemberByRoom(roomId1, memberId1, RoomRole.ADMIN, "Overwritten Name", "New Desc")
            .block()

        StepVerifier.create(memberByRoomRepository.getMemberById(memberId1, roomId1)).assertNext { member ->
                assertEquals("Overwritten Name", member.display_name)
                assertEquals(RoomRole.ADMIN.name, member.role)
            }.verifyComplete()
    }

    @Test
    fun `updateRoom - should return empty if updater does not exist in room`() {
        memberByRoomRepository.createMemberByRoom(roomId1, memberId1, RoomRole.MEMBER, "User", "Desc").block()

        val randomUpdaterId = UUID.randomUUID()

        val updateFlux = memberByRoomRepository.updateRoom(
            description = "New", null, "New", roomId1, randomUpdaterId
        )

        StepVerifier.create(updateFlux).expectNextCount(0).verifyComplete()
    }
}
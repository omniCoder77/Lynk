package com.lynk.messageservice.infrastructure.outbound.persistence.cassandra

import com.lynk.messageservice.domain.model.RoomMember
import com.lynk.messageservice.domain.model.RoomRole
import com.lynk.messageservice.domain.port.driver.MemberByRoomRepository
import com.lynk.messageservice.infrastructure.outbound.persistence.cassandra.entity.MemberByRoom
import com.lynk.messageservice.infrastructure.outbound.persistence.cassandra.entity.MemberByRoomKey
import com.lynk.messageservice.infrastructure.util.BucketUtils.bucket
import org.slf4j.LoggerFactory
import org.springframework.cache.CacheManager
import org.springframework.cache.annotation.Cacheable
import org.springframework.data.cassandra.core.ReactiveCassandraTemplate
import org.springframework.data.cassandra.core.query.Criteria
import org.springframework.data.cassandra.core.query.Query
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers
import java.time.Instant
import java.util.*

@Repository
class MemberByRoomRepositoryImpl(
    private val reactiveCassandraTemplate: ReactiveCassandraTemplate,
    private val cacheManager: CacheManager,
) : MemberByRoomRepository {
    private val logger = LoggerFactory.getLogger(this::class.java)

    override fun createMemberByRoom(
        roomId: UUID, memberId: UUID, role: RoomRole, displayName: String, description: String?
    ): Mono<Boolean> {
        val memberByRoom = MemberByRoom(
            memberByRoomKey = MemberByRoomKey(
                roomId = roomId, memberId = memberId
            ),
            display_name = displayName,
            description = description,
            role = role.name,
            joined_at = Instant.now()
        )
        return reactiveCassandraTemplate.insert(memberByRoom).map { true }.doOnSuccess {
            evictCaches(roomId, memberId)
        }.doOnError { logger.error(it.message, it) }.onErrorReturn(false)
    }

    override fun updateRoom(
        description: String?, avatarUrl: String?, displayName: String?, roomId: UUID, updater: UUID
    ): Flux<UUID> {
        return getMemberById(updater, roomId)
            .filter { it.role == RoomRole.ADMIN.name }
            .flatMapMany { _ ->
                getMembersByRoomId(roomId)
                    .flatMap { memberToUpdate ->
                        val updatedMember = MemberByRoom(
                            memberByRoomKey = MemberByRoomKey(roomId = roomId, memberId = memberToUpdate.memberId),
                            description = description ?: memberToUpdate.description,
                            display_name = displayName ?: memberToUpdate.displayName,
                            role = memberToUpdate.role.name,
                            joined_at = memberToUpdate.joinedAt
                        )
                        reactiveCassandraTemplate.insert(updatedMember)
                            .map { it.memberByRoomKey.memberId }
                            .doOnSuccess { updatedMemberId -> evictCaches(roomId, updatedMemberId) }
                    }
            }
    }

    @Cacheable(value = ["membersByRoom"], key = "#roomId")
    override fun getMembersByRoomId(roomId: UUID): Flux<RoomMember> {
        val query = Query.query(Criteria.where("room_id").`is`(roomId), Criteria.where("bucket").`is`(roomId.bucket()))
        return reactiveCassandraTemplate.select(query, MemberByRoom::class.java)
            .map { it.toDomain() }
    }

    @Cacheable(value = ["memberByRoomAndId"], key = "#roomId + ':' + #inviterId")
    override fun getMemberById(inviterId: UUID, roomId: UUID): Mono<MemberByRoom> {
        val query = Query.query(
            Criteria.where("room_id").`is`(roomId),
            Criteria.where("bucket").`is`(roomId.bucket()),
            Criteria.where("member_id").`is`(inviterId)
        )
        return reactiveCassandraTemplate.selectOne(query, MemberByRoom::class.java)
    }

    override fun deleteMemberByRoomId(roomId: UUID, deleteId: UUID): Mono<Boolean> {
        val key = MemberByRoomKey(roomId = roomId, memberId = deleteId)
        return reactiveCassandraTemplate.deleteById(key, MemberByRoom::class.java)
            .doOnSuccess { if (it) evictCaches(roomId, deleteId) }
    }

    private fun evictCaches(roomId: UUID, memberId: UUID) {
        Mono.fromRunnable<Void> {
            logger.debug("Evicting caches for roomId: $roomId, memberId: $memberId")
            cacheManager.getCache("membersByRoom")?.evict(roomId)
            cacheManager.getCache("memberByRoomAndId")?.evict("$roomId:$memberId")
        }.subscribeOn(Schedulers.boundedElastic()).subscribe()
    }
}
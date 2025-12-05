package com.ethyllium.userservice.domain.port.driven

import com.ethyllium.userservice.domain.model.Member
import com.ethyllium.userservice.domain.model.MemberRole
import reactor.core.publisher.Mono
import java.util.*

interface MemberRepository {
    fun get(memberId: UUID): Mono<Member>
    fun store(member: Member): Mono<UUID>
    fun delete(memberId: UUID): Mono<Boolean>
    fun update(role: MemberRole? = null, memberId: UUID, isAllowedToMessage: Boolean? = null, isAllowedToSendMedia: Boolean? = null): Mono<Boolean>
}
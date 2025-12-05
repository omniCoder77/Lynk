package com.ethyllium.userservice.domain.port.driven

import com.ethyllium.userservice.domain.model.Blocklist
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.UUID

interface BlocklistRepository {
    fun getBlocklists(userId: UUID): Flux<Blocklist>
    fun getBlocklistById(blocklistId: UUID): Mono<Blocklist>
    fun insert(blocklist: Blocklist): Mono<UUID>
    fun delete(blocklistId: UUID): Mono<Boolean>
}
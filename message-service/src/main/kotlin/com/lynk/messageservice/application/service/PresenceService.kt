package com.lynk.messageservice.application.service

import reactor.core.publisher.Mono
import java.util.UUID

interface PresenceService {
    fun setUserOnline(userId: UUID): Mono<Boolean>
    fun setUserOffline(userId: UUID): Mono<Boolean>
    fun isUserOnline(userId: UUID): Mono<Boolean>
}
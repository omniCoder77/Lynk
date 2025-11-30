package com.lynk.messageservice.domain.port.driven

import reactor.core.publisher.Mono

interface OnlineTrackerService {
    fun setUserOnline(userId: String): Mono<Void>
    fun setUserOffline(userId: String): Mono<Void>
    fun isOnline(userId: String): Mono<Boolean>
}
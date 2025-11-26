package com.ethyllium.notificationservice.application.service

import com.ethyllium.notificationservice.domain.exception.TokenNotExistException
import com.ethyllium.notificationservice.domain.port.driven.TokenService
import com.ethyllium.notificationservice.domain.port.driver.UserRepository
import com.ethyllium.notificationservice.infrastructure.outbound.fcm.FCMChatService
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import java.util.*

@Component
class TokenServiceImpl(private val userRepository: UserRepository, private val fCMChatService: FCMChatService) :
    TokenService {
    override fun saveToken(userId: UUID, token: String): Mono<Boolean> {
        return userRepository.addToken(userId, token)
    }

    override fun subscribeTo(userId: UUID, topic: String): Mono<Boolean> {
        return userRepository.find(userId).flatMap { user ->
            if (user.token == null) Mono.error(TokenNotExistException("The FCM Token is null for user $userId"))
            else fCMChatService.subscribeToTopic(user.token, topic).map { it.failureCount == 0 }
        }
    }
}
package com.ethyllium.userservice.application.service

import com.ethyllium.userservice.domain.model.User
import com.ethyllium.userservice.domain.port.driven.BlocklistRepository
import com.ethyllium.userservice.domain.port.driven.UserRepository
import com.ethyllium.userservice.domain.port.driver.UserService
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.UUID

@Service
class UserServiceImpl(private val userRepository: UserRepository, private val blocklistRepository: BlocklistRepository) : UserService {
    override fun find(userId: UUID): Mono<User> {
        return userRepository.find(userId)
    }

    override fun update(
        userId: UUID,
        username: String?,
        bio: String?,
        profile: String?
    ): Mono<Boolean> {
        return userRepository.update(userId, username, bio, profile)
    }

    override fun searchByUsername(username: String, size: Int, offset: Int): Flux<User> {
        return userRepository.findByUsername(username, size, offset)
    }

    override fun getBlockedUsers(userId: UUID): Flux<User> {
        return blocklistRepository.getBlocklists(userId).flatMap { userRepository.find(it.userId) }
    }
}
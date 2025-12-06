package com.ethyllium.userservice.infrastructure.inbound.web

import com.ethyllium.userservice.domain.model.User
import com.ethyllium.userservice.domain.port.driver.UserService
import com.ethyllium.userservice.infrastructure.outbound.security.LynkAuthenticationToken
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono
import java.util.*

@RestController
@RequestMapping("/users")
class UserController(private val userService: UserService) {

    private val logger = LoggerFactory.getLogger(this::class.java)

    @GetMapping("me")
    fun me(authenticationToken: LynkAuthenticationToken): Mono<ResponseEntity<User>> {
        val userId = UUID.fromString(authenticationToken.userId)
        return userService.find(userId).map { ResponseEntity.ok(it) }
            .defaultIfEmpty(ResponseEntity.status(HttpStatus.NOT_FOUND).build())
    }

    @PatchMapping("/me")
    fun update(
        @RequestParam("username", required = false) username: String?,
        authenticationToken: LynkAuthenticationToken,
        @RequestParam("bio", required = false) bio: String?,
        @RequestParam("profile", required = false) profile: String?
    ): Mono<ResponseEntity<String>> {
        val userId = UUID.fromString(authenticationToken.userId)
        return userService.update(userId, username, bio, profile)
            .map { if (it) ResponseEntity.ok().body("User updated") else ResponseEntity.internalServerError().build() }
    }

    @GetMapping("/{userId}")
    fun getUser(@PathVariable userId: String): Mono<ResponseEntity<User>> {
        val userUuid = try {
            UUID.fromString(userId)
        } catch (e: IllegalArgumentException) {
            logger.error("Malformed UUID", e)
            return Mono.just(ResponseEntity.badRequest().build<User>())
        }
        return userService.find(userUuid).map { ResponseEntity.ok(it) }
            .defaultIfEmpty(ResponseEntity.status(HttpStatus.NOT_FOUND).build())
    }

    @GetMapping("/search")
    fun search(
        @RequestParam("username") username: String,
        @RequestParam("page", defaultValue = "0") page: Int,
        @RequestParam("size", defaultValue = "20") size: Int
    ): Mono<ResponseEntity<List<User>>> {
        val offset = page * size
        return userService.searchByUsername(username, size, offset).collectList()
            .map { ResponseEntity.ok(it) }
    }

    @GetMapping("/me/blocked")
    fun getBlockedUsers(authenticationToken: LynkAuthenticationToken): Mono<ResponseEntity<List<User>>> {
        val userId = UUID.fromString(authenticationToken.userId)
        return userService.getBlockedUsers(userId).collectList()
            .map { ResponseEntity.ok(it) }
    }
}
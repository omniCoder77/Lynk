package com.lynk.authservice.infrastructure

import com.lynk.authservice.infrastructure.outbound.jwt.JwtKeyManager
import com.lynk.authservice.infrastructure.outbound.jwt.JwtTokenServiceImpl
import java.util.UUID

fun main() {
    val jwtKeyManager = JwtKeyManager(
        "/home/rishabh/IdeaProjects/Lynk/auth-service/keystore.p12",
        "NJHUy8809889ijKJHYgFfGHJkL878", "jwtKey", "NJHUy8809889ijKJHYgFfGHJkL878"
    )
    val tokenService = JwtTokenServiceImpl(0, 0, jwtKeyManager)
    val userId = UUID.randomUUID().toString()
    println(tokenService.generateTestToken(userId, mapOf("phone_number" to "+1234567890")))
    println(userId)
}
//eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiI4ZWFjNGY2OC01NjE0LTRkMjMtYTY0NS1iNGYwZTU0Y2U5OGYiLCJpYXQiOjE3NjUwNTMyMzV9.D_oFqT8FvHE4frMcKEZeTN46cFCNCRrIclHNn_7PkZg
//8eac4f68-5614-4d23-a645-b4f0e54ce98f
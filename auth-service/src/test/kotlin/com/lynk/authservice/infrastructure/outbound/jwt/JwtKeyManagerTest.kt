package com.lynk.authservice.infrastructure.outbound.jwt

import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec

class JwtKeyManagerTest {
    val jwtKeyManager: JwtKeyManager = JwtKeyManager(
        keyStoreFilePath = System.getenv("JWT_KEYSTORE_LOCATION"),
        keyStorePasswordStr = System.getenv("JWT_KEYSTORE_PASSWORD"),
        keyAlias = "jwtKey",
        keyPasswordStr = System.getenv("JWT_KEY_PASSWORD")
    )

    @Test
    fun `key is initialized`() {
        val key = runBlocking {
            jwtKeyManager.getKey()
        }
        assertNotNull(key, "Key should not be null")
    }

    @Test
    fun `key is created and then initialized`() {
        val key = runBlocking {
            jwtKeyManager.getKey()
        }
        assertNotNull(key, "Key should not be null")
    }

    @Test
    fun `should create a new keystore with generated key`() {
        runBlocking {
            val key = jwtKeyManager.getKey()
            assertThat(key).isInstanceOf(SecretKey::class.java)
            assertThat(key.algorithm).isEqualTo("HmacSHA256")
        }
    }
}
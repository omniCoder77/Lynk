package com.lynk.messageservice.infrastructure.outbound.security

import jakarta.annotation.PostConstruct
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.ClassPathResource
import org.springframework.stereotype.Component
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.security.KeyStore
import javax.crypto.SecretKey

@Component
class JwtKeyProvider(
    @Value("\${jwt.keystore.location}") private val keyStoreLocation: String,
    @Value("\${jwt.keystore.password}") private val keyStorePasswordStr: String,
    @Value("\${jwt.key.alias}") private val keyAlias: String,
    @Value("\${jwt.key.password}") private val keyPasswordStr: String
) {

    private lateinit var keyStore: KeyStore
    private lateinit var cachedKey: SecretKey

    private val logger = LoggerFactory.getLogger(this::class.java)

    init {
        try {
            keyStore = KeyStore.getInstance("PKCS12")
            val keyStoreFileStream: InputStream =
                FileInputStream(File(keyStoreLocation))
            keyStoreFileStream.use {
                keyStore.load(it, keyStorePasswordStr.toCharArray())
            }
            cachedKey = loadKey()
        } catch (e: Exception) {
            logger.error(e.message, e)
            throw IllegalStateException(
                "Failed to load JWT keystore from location: $keyStoreLocation. Ensure the keystore file is present.",
                e
            )
        }
    }

    fun getKey(): SecretKey {
        return cachedKey
    }

    private fun loadKey(): SecretKey {
        val keyProtection = KeyStore.PasswordProtection(keyPasswordStr.toCharArray())
        val keyEntry = keyStore.getEntry(keyAlias, keyProtection)
        return (keyEntry as? KeyStore.SecretKeyEntry)?.secretKey
            ?:run {
                logger.error("Key not found in keystore for alias: $keyAlias")
                throw IllegalStateException("Key not found in keystore for alias: $keyAlias") }
    }
}
package com.lynk.authservice.infrastructure.outbound.jwt

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.security.KeyStore
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey

@Component
class JwtKeyManager(
    @Value("\${jwt.keystore.location:jwtKeystore.jceks}") private var keyStoreFilePath: String,
    @Value("\${jwt.keystore.password}") private var keyStorePasswordStr: String,
    @Value("\${jwt.key.alias:jwtKey}") private var keyAlias: String,
    @Value("\${jwt.key.password}") private var keyPasswordStr: String
) {

    private var keyStore: KeyStore = KeyStore.getInstance("PKCS12")

    init {
        val keyStorePassword = keyStorePasswordStr.toCharArray()

        val keyStoreFile = File(keyStoreFilePath)
        if (keyStoreFile.exists()) {
            FileInputStream(keyStoreFile).use { fis ->
                keyStore.load(fis, keyStorePassword)
            }
        } else {
            keyStore.load(null, keyStorePassword)
            val secretKey = generateSecretKey()
            keyStore.setEntry(
                keyAlias, KeyStore.SecretKeyEntry(secretKey), KeyStore.PasswordProtection(keyPasswordStr.toCharArray())
            )
            FileOutputStream(keyStoreFile).use { fos ->
                keyStore.store(fos, keyStorePassword)
            }
        }
    }

    private fun generateSecretKey(): SecretKey {
        val keyGen = KeyGenerator.getInstance("HmacSHA256")
        keyGen.init(256)
        return keyGen.generateKey()
    }

    @Volatile
    private var cachedKey: SecretKey? = null
    private val keyLoadLock = Mutex()

    suspend fun getKey(): SecretKey = cachedKey ?: keyLoadLock.withLock {
        cachedKey ?: loadKey().also { cachedKey = it }
    }

    private fun loadKey(): SecretKey {
        val keyEntry = keyStore.getEntry(keyAlias, KeyStore.PasswordProtection(keyPasswordStr.toCharArray()))
        return (keyEntry as? KeyStore.SecretKeyEntry)?.secretKey
            ?: throw IllegalStateException("Key not found for alias: $keyAlias")
    }
}
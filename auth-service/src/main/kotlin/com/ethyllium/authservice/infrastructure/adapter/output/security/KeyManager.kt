package com.ethyllium.authservice.infrastructure.adapter.output.security

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.security.KeyStore
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey

@Component
class KeyManager(
    @Value("\${keystore.type}") private val keystoreType: String,
    @Value("\${keystore.path}") private val keystorePath: String,
    @Value("\${keystore.password}") private val keystorePassword: String,
    @Value("\${keystore.alias}") private val keyAlias: String,
) {
    val keyStore: KeyStore = KeyStore.getInstance(keystoreType)

    init {
        if (keystorePath.startsWith("classpath:")) {
            // Load from classpath
            val resourcePath = keystorePath.removePrefix("classpath:")
            val resource = this::class.java.classLoader.getResourceAsStream(resourcePath)
            if (resource != null) {
                keyStore.load(resource, keystorePassword.toCharArray())
            } else {
                initializeNewKeyStore(resourcePath)
            }
        } else {
            // Load from filesystem
            val keyStoreFile = File(keystorePath)
            if (keyStoreFile.exists()) {
                keyStore.load(FileInputStream(keyStoreFile), keystorePassword.toCharArray())
            } else {
                initializeNewKeyStore(keystorePath)
            }
        }
    }

    private fun initializeNewKeyStore(path: String) {
        keyStore.load(null, keystorePassword.toCharArray())
        val secretKey = generateSecretKey()
        keyStore.setEntry(
            keyAlias,
            KeyStore.SecretKeyEntry(secretKey),
            KeyStore.PasswordProtection(keystorePassword.toCharArray())
        )

        if (path.startsWith("classpath:")) {
            val defaultPath = "jwt_key.pkcs12"
            FileOutputStream(defaultPath).use { fos ->
                keyStore.store(fos, keystorePassword.toCharArray())
            }
            println("New keystore created at: ${File(defaultPath).absolutePath}")
        } else {
            // Ensure parent directory exists
            File(path).parentFile?.mkdirs()
            FileOutputStream(path).use { fos ->
                keyStore.store(fos, keystorePassword.toCharArray())
            }
        }
    }

    private fun generateSecretKey(): SecretKey {
        val keyGen = KeyGenerator.getInstance("HmacSHA256")
        keyGen.init(256)
        return keyGen.generateKey()
    }

    fun getKey(): SecretKey {
        val keyEntry = keyStore.getEntry(keyAlias, KeyStore.PasswordProtection(keystorePassword.toCharArray()))
        if (keyEntry is KeyStore.SecretKeyEntry) {
            return keyEntry.secretKey
        }
        throw IllegalStateException("Key not found or wrong type for alias: $keyAlias")
    }
}
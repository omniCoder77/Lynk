package com.lynk.messageservice.application.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.cassandra.CqlSessionBuilderCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.io.FileInputStream
import java.security.KeyStore
import java.security.SecureRandom
import javax.net.ssl.KeyManagerFactory
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManagerFactory

@Configuration
class CassandraConfig(
    @Value("\${spring.cassandra.username}") private val username: String,
    @Value("\${spring.cassandra.password}") private val password: String,
    @Value("\${cassandra.truststore.password}") private val truststorePassword: String,
    @Value("\${cassandra.truststore.location}") private val trustStoreLocation: String,
    @Value("\${cassandra.keystore.location}") private val keyStoreLocation: String,
    @Value("\${cassandra.keystore.password:cassandra}") private val keyStorePassword: String,
) {
    @Bean
    fun cqlSessionBuilderCustomizer(): CqlSessionBuilderCustomizer {
        val sslContext = createSslContext()

        return CqlSessionBuilderCustomizer { builder ->
            builder.withAuthCredentials(username, password).withSslContext(sslContext)
        }
    }

    private fun createSslContext(): SSLContext {
        val trustStorePassChars = truststorePassword.toCharArray()
        val keyStorePassChars = keyStorePassword.toCharArray() // Get Keystore password

        val trustStore = KeyStore.getInstance("JKS")
        FileInputStream(trustStoreLocation).use { fis ->
            trustStore.load(fis, trustStorePassChars)
        }
        val trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
        trustManagerFactory.init(trustStore)

        val keyStore = KeyStore.getInstance("PKCS12")
        FileInputStream(keyStoreLocation).use { fis ->
            keyStore.load(fis, keyStorePassChars)
        }

        val keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm())
        keyManagerFactory.init(keyStore, keyStorePassChars)

        val sslContext = SSLContext.getInstance("TLS")

        sslContext.init(
            keyManagerFactory.keyManagers,
            trustManagerFactory.trustManagers,
            SecureRandom()
        )

        return sslContext
    }
}
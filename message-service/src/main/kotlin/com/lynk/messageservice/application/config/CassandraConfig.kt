package com.lynk.messageservice.application.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.cassandra.CqlSessionBuilderCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.io.FileInputStream
import java.security.KeyStore
import java.security.SecureRandom
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManagerFactory

@Configuration
class CassandraConfig(
    @Value("\${spring.cassandra.username}") private val username: String,
    @Value("\${spring.cassandra.password}") private val password: String,
) {
    @Bean
    fun cqlSessionBuilderCustomizer(): CqlSessionBuilderCustomizer {
        val sslContext = createSslContext()

        return CqlSessionBuilderCustomizer { builder ->
            builder.withAuthCredentials(username, "message-user-password").withSslContext(sslContext)
        }
    }

    private fun createSslContext(): SSLContext {
        val truststorePassword = "cassandra".toCharArray()

        val trustStore = KeyStore.getInstance("JKS")
        FileInputStream("cassandra.truststore.jks").use { fis ->
            trustStore.load(fis, truststorePassword)
        }

        val trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
        trustManagerFactory.init(trustStore)

        val sslContext = SSLContext.getInstance("TLS")
        sslContext.init(
            null, trustManagerFactory.trustManagers, SecureRandom()
        )

        return sslContext
    }
}
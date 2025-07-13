package com.ethyllium.messageservice.infrastructure.config

import com.datastax.oss.driver.api.core.CqlSessionBuilder
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.cassandra.CqlSessionBuilderCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import kotlin.io.path.Path

@Configuration
class AstraDbSpringDataConfig(
    @Value("\${datastax.astra.secure-connect-bundle}") private val cloudSecureBundlePath: String
) {

    @Bean
    fun sessionBuilderCustomizer(): CqlSessionBuilderCustomizer {
        return CqlSessionBuilderCustomizer { builder: CqlSessionBuilder ->
            builder.withCloudSecureConnectBundle(
                Path(cloudSecureBundlePath)
            )
        }
    }
}
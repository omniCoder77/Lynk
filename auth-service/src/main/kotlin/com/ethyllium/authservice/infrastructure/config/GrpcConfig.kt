package com.ethyllium.authservice.infrastructure.config

import io.grpc.ServerInterceptor
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.ImportAutoConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.grpc.autoconfigure.server.GrpcServerAutoConfiguration
import org.springframework.grpc.autoconfigure.server.GrpcServerFactoryAutoConfiguration
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder

@Configuration
@ImportAutoConfiguration(classes = [GrpcServerAutoConfiguration::class, GrpcServerFactoryAutoConfiguration::class])
class GrpcConfig {

    @Value("\${grpc.server.port:9090}")
    private val grpcPort: Int = 9090

    @Bean
    fun passwordEncoder(): PasswordEncoder {
        return BCryptPasswordEncoder(12)
    }

    /**
     * Logging interceptor for gRPC calls
     */
    @Bean
    fun loggingInterceptor(): ServerInterceptor {
        return LoggingServerInterceptor()
    }
}
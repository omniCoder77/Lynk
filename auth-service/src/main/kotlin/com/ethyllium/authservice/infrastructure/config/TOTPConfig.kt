package com.ethyllium.authservice.infrastructure.config

import org.apache.commons.codec.binary.Base32
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class TOTPConfig {
    @Bean
    fun base32(): Base32 {
        return Base32()
    }
}

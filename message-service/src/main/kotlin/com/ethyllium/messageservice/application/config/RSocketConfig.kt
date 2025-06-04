package com.ethyllium.messageservice.application.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.messaging.rsocket.RSocketStrategies

@Configuration
class RSocketConfig {
    @Bean
    fun rSocketStrategies(): RSocketStrategies = RSocketStrategies.builder().build()
}
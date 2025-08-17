package com.lynk.authservice.application.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.ClassPathResource
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.data.redis.core.script.RedisScript
import org.springframework.data.redis.serializer.GenericToStringSerializer
import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer
import org.springframework.data.redis.serializer.RedisSerializationContext
import org.springframework.data.redis.serializer.StringRedisSerializer


@Configuration
class RedisConfig {
    @Bean
    fun reactiveRedisTemplate(factory: ReactiveRedisConnectionFactory): ReactiveRedisTemplate<String, String> {
        val jdkSerializationRedisSerializer = JdkSerializationRedisSerializer()
        val stringRedisSerializer = StringRedisSerializer.UTF_8
        val stringSerializer = GenericToStringSerializer(String::class.java)
        val template = ReactiveRedisTemplate(
            factory,
            RedisSerializationContext.newSerializationContext<String, String>(jdkSerializationRedisSerializer)
                .key(stringRedisSerializer).value(stringSerializer).build()
        )
        return template
    }

    @Bean
    fun script(): RedisScript<Boolean> {
        return RedisScript.of(ClassPathResource("scripts/rateLimiter.lua"), Boolean::class.java)
    }

    @Bean
    fun reactiveRedisTemplateRateLimit(factory: ReactiveRedisConnectionFactory): ReactiveRedisTemplate<String, Long> {
        val jdkSerializationRedisSerializer = JdkSerializationRedisSerializer()
        val stringRedisSerializer = StringRedisSerializer.UTF_8
        val longToStringSerializer = GenericToStringSerializer(Long::class.java)
        val template = ReactiveRedisTemplate(
            factory,
            RedisSerializationContext.newSerializationContext<String?, Long?>(jdkSerializationRedisSerializer)
                .key(stringRedisSerializer).value(longToStringSerializer).build()
        )
        return template
    }
}
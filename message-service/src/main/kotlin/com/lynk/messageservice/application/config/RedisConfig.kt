package com.lynk.messageservice.application.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.ClassPathResource
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.data.redis.core.script.RedisScript
import org.springframework.data.redis.serializer.GenericToStringSerializer
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer
import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer
import org.springframework.data.redis.serializer.RedisSerializationContext
import org.springframework.data.redis.serializer.StringRedisSerializer
import org.springframework.web.reactive.socket.WebSocketSession
import redis.clients.jedis.HostAndPort
import redis.clients.jedis.Jedis
import redis.clients.jedis.UnifiedJedis
import kotlin.jvm.java

@Configuration
class RedisConfig {
    @Bean
    fun reactiveRedisTemplate(factory: ReactiveRedisConnectionFactory): ReactiveRedisTemplate<String, String> {
        val jdkSerializationRedisSerializer = Jackson2JsonRedisSerializer(Object::class.java)
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
        val jdkSerializationRedisSerializer = Jackson2JsonRedisSerializer(Object::class.java)
        val stringRedisSerializer = StringRedisSerializer.UTF_8
        val longToStringSerializer = GenericToStringSerializer(Long::class.java)
        val template = ReactiveRedisTemplate(
            factory,
            RedisSerializationContext.newSerializationContext<String?, Long?>(jdkSerializationRedisSerializer)
                .key(stringRedisSerializer).value(longToStringSerializer).build()
        )
        return template
    }

    @Bean
    fun jedis(@Value("\${spring.data.redis.host}") host: String, @Value("\${spring.data.redis.port}") port: Int): UnifiedJedis {
        val jedis = UnifiedJedis(HostAndPort(host, port))
        return jedis
    }

    @Bean
    fun userRegisteredEventTemplate(factory: ReactiveRedisConnectionFactory): ReactiveRedisTemplate<String, WebSocketSession> {
        val keySerializer = StringRedisSerializer.UTF_8

        val objectMapper = ObjectMapper().registerModule(KotlinModule.Builder().build())
        val valueSerializer = Jackson2JsonRedisSerializer(objectMapper, WebSocketSession::class.java)

        val builder = RedisSerializationContext.newSerializationContext<String, WebSocketSession>(keySerializer)
        val context = builder.value(valueSerializer).build()

        return ReactiveRedisTemplate(factory, context)
    }
}
package com.lynk.messageservice.application.config

import org.springframework.cache.CacheManager
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.cache.RedisCacheConfiguration
import org.springframework.data.redis.cache.RedisCacheManager
import org.springframework.data.redis.connection.RedisConnectionFactory
import java.util.Collections

@Configuration
class CacheConfig {

    @Bean
    fun cacheManager(redisConnectionFactory: RedisConnectionFactory): CacheManager {
        val cacheManager: RedisCacheManager = RedisCacheManager.builder(redisConnectionFactory)
            .cacheDefaults(RedisCacheConfiguration.defaultCacheConfig())
            .transactionAware()
            .withInitialCacheConfigurations(
                Collections.singletonMap(
                    "predefined",
                    RedisCacheConfiguration.defaultCacheConfig().disableCachingNullValues()
                )
            )
            .build()
        return cacheManager
    }
}
package com.lynk.messageservice.infrastructure.outbound.persistence.redis

import com.lynk.messageservice.domain.port.driven.CuckooFilter
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import redis.clients.jedis.UnifiedJedis

@Component
class RedisCuckooFilter(private val jedis: UnifiedJedis) : CuckooFilter {
    override fun exists(key: String, item: String): Boolean {
        return jedis.cfExists(key, item)
    }

    override fun exists(key: String, vararg item: String): List<Boolean> {
        return jedis.cfMExists(key, *item)
    }

    override fun add(table: String, entity: String): Boolean {
        return jedis.cfAdd(table, entity)
    }

    override fun remove(table: String, entity: String): Boolean {
        return jedis.cfDel(table, entity)
    }

    override fun add(key: String, vararg items: String): List<Boolean> {
        return jedis.cfInsert(key, *items)
    }
}
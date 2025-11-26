package com.lynk.messageservice.domain.port.driven

import reactor.core.publisher.Mono

interface CuckooFilter {

    fun exists(key: String, item: String): Boolean
    fun exists(key: String, vararg item: String): List<Boolean>
    fun add(table: String, entity: String): Boolean
    fun remove(table: String, entity: String): Boolean
    fun add(key: String, vararg items: String): List<Boolean>
}

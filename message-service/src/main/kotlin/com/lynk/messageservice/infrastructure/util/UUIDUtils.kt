package com.lynk.messageservice.infrastructure.util

import java.util.UUID

object UUIDUtils {
    fun getConversationId(user1: String, user2: String): UUID {
        val sorted = listOf(user1, user2).sorted()
        val key = "${sorted[0]}:${sorted[1]}"
        return UUID.nameUUIDFromBytes(key.toByteArray())
    }
}
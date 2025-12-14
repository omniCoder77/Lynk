package com.ethyllium.roomservice.infrastructure.util

import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.security.MessageDigest
import java.util.*

// Fixed namespace UUID for conversation IDs.
// DO NOT CHANGE once deployed.
private val CONVERSATION_NAMESPACE = UUID.fromString("12345678-1234-5678-1234-567812345678")

fun uuid5(namespace: UUID, name: String): UUID {
    val sha1 = MessageDigest.getInstance("SHA-1")

    // Convert namespace UUID → 16-byte array (RFC 4122 §4.3)
    val nsBytes =
        ByteBuffer.allocate(16).putLong(namespace.mostSignificantBits).putLong(namespace.leastSignificantBits).array()

    // RFC 4122 namespace-based UUIDs hash: hash(namespace || name)
    sha1.update(nsBytes)
    val hash = sha1.digest(name.toByteArray(Charsets.UTF_8))

    // ---- RFC 4122 REQUIRED BIT MANIPULATIONS ----

    // Version (4 bits in octet 6) — RFC 4122 §4.1.3
    // 0x0F = 0000 1111 → clears the top 4 bits (version)
    // 5 << 4 = 0101 0000 → sets version = 5 (SHA-1 name-based)
    hash[6] = ((hash[6].toInt() and 0x0F) or (5 shl 4)).toByte()

    // Variant (2–3 MSBs in octet 8) — RFC 4122 §4.1.1
    // 0x3F = 0011 1111 → clears the top 2 bits
    // 0x80 = 1000 0000 → sets variant to IETF (10xxxxxx)
    hash[8] = ((hash[8].toInt() and 0x3F) or 0x80).toByte()

    val bb = ByteBuffer.wrap(hash, 0, 16).order(ByteOrder.BIG_ENDIAN)

    return UUID(bb.long, bb.long)

}

object UUIDUtils {
    fun merge(uuid1: String, uuid2: String, ordered: Boolean = true): UUID {
        val sorted = listOf(uuid1, uuid2).sorted()
        val key = if (ordered) "${sorted[0]}:${sorted[1]}" else "$uuid1:$uuid2"
        return uuid5(CONVERSATION_NAMESPACE, key)
    }
}
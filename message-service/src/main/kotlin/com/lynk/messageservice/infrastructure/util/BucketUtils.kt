package com.lynk.messageservice.infrastructure.util

import java.time.Instant
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit
import java.util.UUID

object BucketUtils {

    fun Instant.toTimeBucket(): String {
        val dateTime = this.atZone(ZoneOffset.UTC)
        return "${dateTime.year}-${dateTime.monthValue.toString().padStart(2, '0')}"
    }

    fun UUID.bucket(bucketCount: Int = 10): Int {
        return (this.hashCode() and Int.MAX_VALUE) % bucketCount
    }

    fun generateTimeBuckets(start: Instant, end: Instant): List<String> {
        val buckets = mutableListOf<String>()
        var current = start.atZone(ZoneOffset.UTC).truncatedTo(ChronoUnit.DAYS).withDayOfMonth(1)
        val endZoned = end.atZone(ZoneOffset.UTC)

        while (current.isBefore(endZoned) || current.month == endZoned.month) {
            val bucket = "${current.year}-${current.monthValue.toString().padStart(2, '0')}"
            buckets.add(bucket)
            current = current.plusMonths(1)
        }
        return buckets
    }
}
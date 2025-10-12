package com.lynk.messageservice.infrastructure.util

import java.time.Instant
import java.time.ZoneOffset

object BucketUtils {
    
    fun Instant.bucket(): Int {
        val dateTime = this.atZone(ZoneOffset.UTC)
        return dateTime.year * 100 + dateTime.monthValue
    }

    fun bucketsInRange(start: Instant, end: Instant): List<Int> {
        val buckets = mutableListOf<Int>()
        var current = start.atZone(ZoneOffset.UTC).toLocalDate().withDayOfMonth(1)
        val endDate = end.atZone(ZoneOffset.UTC).toLocalDate()

        while (!current.isAfter(endDate)) {
            buckets.add(current.year * 100 + current.monthValue)
            current = current.plusMonths(1)
        }

        return buckets
    }
}
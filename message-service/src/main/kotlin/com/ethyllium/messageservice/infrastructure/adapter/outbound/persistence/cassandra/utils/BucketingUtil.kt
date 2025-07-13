package com.ethyllium.messageservice.infrastructure.adapter.outbound.persistence.cassandra.utils

import java.time.Instant
import java.time.YearMonth
import java.time.ZoneOffset
import kotlin.math.abs

class BucketingUtil {
    companion object {
        fun calculateBucket(senderId: String, instant: Instant): Int {
        val month = YearMonth.from(instant.atZone(ZoneOffset.UTC))
        val hash = abs(senderId.hashCode() % 4)
        return (month.year * 100 + month.monthValue) * 10 + hash
    }
    }
}
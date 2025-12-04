package com.lynk.mediaservice.domain.exception

data class CacheDeserializationException(private val string: String, private val e: Exception): RuntimeException(string, e)
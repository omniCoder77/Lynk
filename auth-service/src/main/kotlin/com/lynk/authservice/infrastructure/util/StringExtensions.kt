package com.lynk.authservice.infrastructure.util

fun String.toBearerToken(): String = this.substringAfter("Bearer ")
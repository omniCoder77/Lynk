package com.lynk.mediaservice.domain.exception

class ProfileUploadException(
    message: String,
    val filename: String,
    val s3Key: String,
    cause: Throwable? = null
) : RuntimeException("$message [filename=$filename, s3Key=$s3Key]", cause)

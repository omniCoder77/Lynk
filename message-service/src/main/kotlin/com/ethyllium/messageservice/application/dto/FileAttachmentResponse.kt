package com.ethyllium.messageservice.application.dto

import java.time.Instant

data class FileAttachmentResponse(
    val fileId: String,
    val fileName: String,
    val contentType: String,
    val fileSize: Long,
    val url: String,
    val conversationId: String,
    val uploaderId: String,
    val uploadTime: Instant,
    val messageReference: String?
)

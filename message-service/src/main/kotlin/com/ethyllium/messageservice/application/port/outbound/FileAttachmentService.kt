package com.ethyllium.messageservice.application.port.outbound

import com.ethyllium.messageservice.application.dto.FileAttachmentResponse
import org.springframework.web.multipart.MultipartFile

interface FileAttachmentService {
    fun uploadFile(
        userId: String,
        file: MultipartFile,
        conversationId: String,
        messageReference: String? = null
    ): FileAttachmentResponse

    fun getFile(
        userId: String, fileId: String, conversationId: String, messageReference: String?
    ): FileAttachmentResponse

    fun deleteFile(userId: String, fileId: String)
}
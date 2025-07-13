package com.ethyllium.messageservice.application.service

import com.ethyllium.messageservice.application.dto.FileAttachmentResponse
import com.ethyllium.messageservice.application.port.outbound.FileAttachmentService
import com.ethyllium.messageservice.application.port.outbound.FileStoragePort
import org.springframework.stereotype.Component
import org.springframework.web.multipart.MultipartFile
import java.time.Instant

@Component
class FileAttachmentServiceImpl(
    private val fileStoragePort: FileStoragePort
) : FileAttachmentService {

    override fun uploadFile(
        userId: String,
        file: MultipartFile,
        conversationId: String,
        messageReference: String?
    ): FileAttachmentResponse {
        val storedFile = fileStoragePort.store(userId, file)

        return FileAttachmentResponse(
            fileId = storedFile.fileId,
            fileName = storedFile.fileName,
            contentType = storedFile.mimeType,
            fileSize = storedFile.size,
            url = storedFile.url,
            conversationId = conversationId,
            uploaderId = userId,
            uploadTime = Instant.now(),
            messageReference = messageReference
        )
    }

    override fun getFile(userId: String, fileId: String,
        conversationId: String,
        messageReference: String?): FileAttachmentResponse {
        val storedFile = fileStoragePort.retrieve(userId, fileId)
        return FileAttachmentResponse(
            fileId = storedFile.fileId,
            fileName = storedFile.fileName,
            contentType = storedFile.mimeType,
            fileSize = storedFile.size,
            url = storedFile.url,
            conversationId = conversationId,
            uploaderId = userId,
            uploadTime = Instant.now(),
            messageReference = messageReference
        )
    }

    override fun deleteFile(userId: String, fileId: String) {
        fileStoragePort.delete(userId, fileId)
    }
}

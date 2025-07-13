package com.ethyllium.messageservice.infrastructure.adapter.outbound.persistence.storage

import com.ethyllium.messageservice.application.port.outbound.FileStoragePort
import com.ethyllium.messageservice.application.port.outbound.StoredFileInfo
import com.ethyllium.messageservice.infrastructure.adapter.outbound.persistence.storage.util.FilePathUtil
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.multipart.MultipartFile
import java.io.FileNotFoundException
import java.nio.file.Files
import java.util.*

@Component
class DiskFileStorageAdapter(
    @Value("\${file.upload-dir}") private val uploadDir: String
) : FileStoragePort {
    override fun store(userId: String, file: MultipartFile): StoredFileInfo {
        val fileId = UUID.randomUUID().toString()
        val fileName = file.originalFilename ?: "file"
        val storagePath = FilePathUtil.getPath(userId, userId, fileId)

        Files.createDirectories(storagePath.parent)
        file.transferTo(storagePath)

        return StoredFileInfo(
            fileId = fileId,
            fileName = fileName,
            mimeType = file.contentType ?: "application/octet-stream",
            size = file.size,
            url = "/files/$userId/$fileId"
        )
    }

    override fun retrieve(
        userId: String, fileId: String
    ): StoredFileInfo {
        val file = FilePathUtil.getPath(uploadDir, userId, fileId).toFile()
        if (!file.exists()) throw FileNotFoundException("File not found: $fileId")
        return StoredFileInfo(
            fileId = fileId,
            fileName = file.name,
            mimeType = Files.probeContentType(file.toPath()) ?: "application/octet-stream",
            size = file.length(),
            url = "/files/$userId/$fileId"
        )
    }

    override fun delete(userId: String, fileId: String) {
        TODO("Not yet implemented")
    }
}
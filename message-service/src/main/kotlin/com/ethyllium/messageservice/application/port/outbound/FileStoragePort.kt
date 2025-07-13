package com.ethyllium.messageservice.application.port.outbound

import org.springframework.web.multipart.MultipartFile

interface FileStoragePort {
    fun store(userId: String, file: MultipartFile): StoredFileInfo
    fun retrieve(userId: String, fileId: String): StoredFileInfo
    fun delete(userId: String, fileId: String)
}

data class StoredFileInfo(
    val fileId: String,
    val fileName: String,
    val mimeType: String,
    val size: Long,
    val url: String // or path if not exposed publicly
)
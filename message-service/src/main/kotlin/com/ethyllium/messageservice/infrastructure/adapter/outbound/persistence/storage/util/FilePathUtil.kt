package com.ethyllium.messageservice.infrastructure.adapter.outbound.persistence.storage.util

import java.nio.file.Path
import java.nio.file.Paths

class FilePathUtil {
    companion object {
        fun getPath(
            uploadDir: String,
            userId: String,
            fileId: String,
        ): Path {
            return Paths.get(uploadDir, userId, fileId)
        }
    }
}
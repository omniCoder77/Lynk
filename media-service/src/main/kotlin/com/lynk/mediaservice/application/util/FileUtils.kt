package com.lynk.mediaservice.application.util

import org.springframework.core.io.buffer.DataBufferUtils
import org.springframework.http.codec.multipart.FilePart
import reactor.core.publisher.Mono

object FileUtils {
    val USER_PROFILE_DIRECTORY = "user-profile"
    val ROOM_PROFILE_DIRECTORY = "user-profile"

    private val PNG_SIGNATURE = byteArrayOf(
        0x89.toByte(), 0x50, 0x4E, 0x47,
        0x0D, 0x0A, 0x1A, 0x0A
    )

    fun validatePng(filePart: FilePart): Mono<Boolean> {
        return filePart.content()
            .next()
            .map { buffer ->
                val bytes = ByteArray(8)
                buffer.read(bytes)
                DataBufferUtils.release(buffer)
                PNG_SIGNATURE.contentEquals(bytes)
            }
    }
}
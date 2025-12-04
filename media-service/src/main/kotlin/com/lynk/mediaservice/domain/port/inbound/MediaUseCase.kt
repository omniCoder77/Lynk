package com.lynk.mediaservice.domain.port.inbound

import com.lynk.mediaservice.domain.model.MediaFile
import org.springframework.http.codec.multipart.FilePart
import reactor.core.publisher.Mono

interface MediaUseCase {
    fun uploadUserProfile(filePart: FilePart, userId: String): Mono<String>
    fun downloadMedia(fileName: String): Mono<MediaFile>
    fun deleteMedia(fileName: String): Mono<Boolean>
    fun updateMedia(fileName: String, filePart: FilePart): Mono<Boolean>
    fun uploadRoomProfile(filePart: FilePart, userId: String): Mono<String>
}
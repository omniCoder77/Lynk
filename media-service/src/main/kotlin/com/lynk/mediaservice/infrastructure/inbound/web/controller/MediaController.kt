package com.lynk.mediaservice.infrastructure.inbound.web.controller

import com.lynk.mediaservice.domain.port.inbound.MediaUseCase
import org.springframework.core.io.ByteArrayResource
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.http.codec.multipart.FilePart
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/api/v1/media")
class MediaController(
    private val mediaUseCase: MediaUseCase
) {

    @PostMapping(consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    fun uploadFile(@RequestPart("file") file: Mono<FilePart>): Mono<ResponseEntity<Map<String, String>>> {
        return file.flatMap { filePart ->
            mediaUseCase.uploadMedia(filePart)
                .map { fileName ->
                    ResponseEntity.ok(mapOf("message" to "File uploaded successfully", "fileName" to fileName))
                }
        }
    }

    @GetMapping("/{fileName}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    fun downloadFile(@PathVariable fileName: String): Mono<ResponseEntity<ByteArrayResource>> {
        return mediaUseCase.downloadMedia(fileName)
            .map { mediaFile ->
                val resource = ByteArrayResource(mediaFile.content ?: ByteArray(0))

                ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"${mediaFile.fileName}\"")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(resource)
            }
    }

    @PutMapping("/{fileName}", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    fun updateFile(
        @PathVariable fileName: String,
        @RequestPart("file") file: Mono<FilePart>
    ): Mono<ResponseEntity<Map<String, String>>> {
        return file.flatMap { filePart ->
            mediaUseCase.updateMedia(fileName, filePart)
                .flatMap { success ->
                    if (success) {
                        Mono.just(ResponseEntity.ok(mapOf("message" to "File updated successfully")))
                    } else {
                        Mono.just(ResponseEntity.badRequest().body(mapOf("message" to "Failed to update file")))
                    }
                }
        }
    }

    @DeleteMapping("/{fileName}")
    @PreAuthorize("hasRole('ADMIN')")
    fun deleteFile(@PathVariable fileName: String): Mono<ResponseEntity<Map<String, String>>> {
        return mediaUseCase.deleteMedia(fileName)
            .flatMap { success ->
                if (success) {
                    Mono.just(ResponseEntity.ok(mapOf("message" to "File deleted successfully")))
                } else {
                    Mono.just(ResponseEntity.badRequest().body(mapOf("message" to "Failed to delete file or file not found")))
                }
            }
    }
}
package com.lynk.mediaservice.application.service

import com.lynk.mediaservice.domain.model.MediaFile
import com.lynk.mediaservice.domain.port.driver.StorageGateway
import com.lynk.mediaservice.domain.port.inbound.MediaUseCase
import org.slf4j.LoggerFactory
import org.springframework.http.codec.multipart.FilePart
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers
import java.nio.file.Files
import java.nio.file.Path

@Service
class MediaService(
    private val storageGateway: StorageGateway
) : MediaUseCase {

    private val logger = LoggerFactory.getLogger(this::class.java)

    override fun uploadMedia(filePart: FilePart): Mono<String> {
        return createTempFile(filePart)
            .flatMap { tempFile ->
                val originalName = filePart.filename()
                val finalFile = tempFile.resolveSibling(originalName).toFile()
                tempFile.toFile().renameTo(finalFile)

                storageGateway.upload(finalFile)
                    .doOnTerminate {
                        // Cleanup temp file after upload completes or fails
                        finalFile.delete()
                    }
                    .map { success ->
                        if (success) originalName else throw RuntimeException("Upload failed")
                    }
            }
    }

    override fun downloadMedia(fileName: String): Mono<MediaFile> {
        return storageGateway.read(fileName)
            .map { bytes ->
                MediaFile(fileName = fileName, content = bytes)
            }
            .switchIfEmpty(Mono.error(RuntimeException("File not found: $fileName")))
    }

    override fun deleteMedia(fileName: String): Mono<Boolean> {
        return storageGateway.delete(fileName)
    }

    override fun updateMedia(fileName: String, filePart: FilePart): Mono<Boolean> {
        return createTempFile(filePart)
            .flatMap { tempFile ->
                storageGateway.update(fileName, tempFile.toFile())
                    .publishOn(Schedulers.boundedElastic())
                    .doOnTerminate {
                        Files.deleteIfExists(tempFile)
                    }
            }
    }

    private fun createTempFile(filePart: FilePart): Mono<Path> {
        return Mono.fromCallable {
            Files.createTempFile("lynk-upload-", ".tmp")
        }.publishOn(Schedulers.boundedElastic()).flatMap { tempPath ->
            filePart.transferTo(tempPath)
                .thenReturn(tempPath)
        }
    }
}
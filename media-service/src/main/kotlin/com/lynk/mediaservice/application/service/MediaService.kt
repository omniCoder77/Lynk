package com.lynk.mediaservice.application.service

import com.lynk.mediaservice.application.util.CacheUtils
import com.lynk.mediaservice.application.util.FileUtils
import com.lynk.mediaservice.domain.exception.ImageFormatException
import com.lynk.mediaservice.domain.exception.ProfileUploadException
import com.lynk.mediaservice.domain.exception.UnauthorizedRoomActionException
import com.lynk.mediaservice.domain.model.MediaFile
import com.lynk.mediaservice.domain.port.driver.CacheRepository
import com.lynk.mediaservice.domain.port.driver.StorageGateway
import com.lynk.mediaservice.domain.port.inbound.MediaUseCase
import org.slf4j.LoggerFactory
import org.springframework.http.codec.multipart.FilePart
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers
import java.nio.file.Files

@Service
class MediaService(
    private val storageGateway: StorageGateway, private val cacheRepository: CacheRepository
) : MediaUseCase {

    private val logger = LoggerFactory.getLogger(this::class.java)

    override fun uploadUserProfile(filePart: FilePart, userId: String): Mono<String> {
        return FileUtils.validatePng(filePart).filter { it }
            .switchIfEmpty(Mono.error(ImageFormatException("Invalid image format: expected PNG"))).flatMap {
                uploadFile(filePart, "${FileUtils.USER_PROFILE_DIRECTORY}/$userId")
            }
    }

    override fun uploadRoomProfile(filePart: FilePart, userId: String): Mono<String> {
        return FileUtils.validatePng(filePart).filter { it }
            .switchIfEmpty(Mono.error(ImageFormatException("Invalid image format: expected PNG"))).flatMap {
                cacheRepository.get(CacheUtils.ROOM_PROFILE_KEY_PREFIX + userId, String::class.java).switchIfEmpty(Mono.error(
                    RuntimeException("Cache entry not found. Please request a pre-signed URL first."))).flatMap { cachedValue ->
                        val parts = cachedValue.split(CacheUtils.CACHE_VALUE_DELIMITER, limit =  2)
                        val cacheUserId = parts[0]
                        val roomId = parts[1]
                        if (cacheUserId == userId) uploadFile(filePart, "${FileUtils.ROOM_PROFILE_DIRECTORY}/$roomId")
                        else Mono.error(UnauthorizedRoomActionException("profile.update"))
                    }
            }
    }

    override fun downloadMedia(fileName: String): Mono<MediaFile> {
        return storageGateway.read(fileName).map { bytes ->
            MediaFile(fileName = fileName, content = bytes)
        }.switchIfEmpty(Mono.error(RuntimeException("File not found: $fileName")))
    }

    override fun deleteMedia(fileName: String): Mono<Boolean> {
        return storageGateway.delete(fileName)
    }

    private fun uploadFile(filePart: FilePart, fileName: String): Mono<String> {
        return Mono.fromCallable {
            Files.createTempFile(fileName, ".tmp")
        }.publishOn(Schedulers.boundedElastic()).flatMap { tempPath ->
            filePart.transferTo(tempPath).thenReturn(tempPath).publishOn(Schedulers.boundedElastic()).doFinally {
                try {
                    Files.deleteIfExists(tempPath)
                } catch (e: Exception) {
                    logger.warn("Failed to delete temp file: $tempPath", e)
                }
            }.flatMap { _ ->
                Mono.just(
                    tempPath.toFile() to fileName
                )
            }.flatMap { (fileToUpload, s3Key) ->
                storageGateway.upload(fileToUpload, s3Key).flatMap { success ->
                    if (success) {
                        Mono.just(s3Key)
                    } else {
                        Mono.error(
                            ProfileUploadException(
                                "Failed to upload file to S3", filename = filePart.filename(), s3Key = s3Key
                            )
                        )
                    }
                }
            }
        }
    }

    override fun updateMedia(fileName: String, filePart: FilePart): Mono<Boolean> {
        return Mono.fromCallable {
            Files.createTempFile("lynk-upload-", ".tmp")
        }.publishOn(Schedulers.boundedElastic()).flatMap { tempPath ->
            filePart.transferTo(tempPath).thenReturn(tempPath)
        }.flatMap { tempFile ->
            storageGateway.update(fileName, tempFile.toFile()).publishOn(Schedulers.boundedElastic()).doOnTerminate {
                Files.deleteIfExists(tempFile)
            }
        }
    }
}
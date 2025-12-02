package com.lynk.mediaservice.infrastructure.outbound.s3

import com.lynk.mediaservice.domain.port.driver.StorageGateway
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import software.amazon.awssdk.core.async.AsyncRequestBody
import software.amazon.awssdk.core.async.AsyncResponseTransformer
import software.amazon.awssdk.services.s3.S3AsyncClient
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest
import software.amazon.awssdk.services.s3.model.GetObjectRequest
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import java.io.File

@Component
class AWSS3StorageGateway(
    private val s3AsyncClient: S3AsyncClient, @Value("\${aws.s3.bucket.name}") private val s3Bucket: String
) : StorageGateway {
    private val logger = LoggerFactory.getLogger(this::class.java)

    override fun upload(file: File): Mono<Boolean> {
        return Mono.fromFuture {
            s3AsyncClient.putObject(
                PutObjectRequest.builder().bucket(s3Bucket).key(file.name).build(), AsyncRequestBody.fromFile(file)
            )
        }.map { response -> response.sdkHttpResponse().isSuccessful }
            .doOnError { e -> logger.error("Upload failed", e) }.onErrorReturn(false)
    }

    override fun delete(fileName: String): Mono<Boolean> {
        if (fileName.isBlank()) {
            return Mono.just(false)
        }

        return Mono.fromFuture {
            s3AsyncClient.deleteObject(
                DeleteObjectRequest.builder().bucket(s3Bucket).key(fileName).build()
            )
        }.map { response -> response.sdkHttpResponse().isSuccessful }.doOnNext { successful ->
            if (successful) {
                logger.info("Successfully deleted file '$fileName' from bucket '$s3Bucket'")
            }
        }.doOnError { e -> logger.error("Delete failed for '$fileName'", e) }.onErrorReturn(false)
    }

    override fun read(fileName: String): Mono<ByteArray> {
        if (fileName.isBlank()) {
            return Mono.empty()
        }

        return Mono.fromFuture {
            s3AsyncClient.getObject(
                GetObjectRequest.builder().bucket(s3Bucket).key(fileName).build(), AsyncResponseTransformer.toBytes()
            )
        }.map { response ->
            response.asByteArray()
        }.doOnNext {
            logger.info("Successfully read file '$fileName' from bucket '$s3Bucket'")
        }.doOnError { e ->
            logger.error("Read failed for '$fileName'", e)
        }
    }

    override fun update(fileName: String, file: File): Mono<Boolean> {
        if (fileName.isBlank()) {
            logger.error("File name cannot be blank")
            return Mono.just(false)
        }

        return Mono.fromFuture {
            s3AsyncClient.putObject(
                PutObjectRequest.builder().bucket(s3Bucket).key(fileName).build(), AsyncRequestBody.fromFile(file)
            )
        }.map { response ->
            response.sdkHttpResponse().isSuccessful
        }.doOnNext { successful ->
            if (successful) {
                logger.info("Successfully updated file '$fileName' in bucket '$s3Bucket'")
            }
        }.doOnError { e ->
            logger.error("Update failed for '$fileName'", e)
        }.onErrorReturn(false)
    }

}
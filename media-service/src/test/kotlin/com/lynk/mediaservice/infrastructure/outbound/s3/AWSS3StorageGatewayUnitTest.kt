package com.lynk.mediaservice.infrastructure.outbound.s3

import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.io.TempDir
import org.mockito.ArgumentMatchers.any
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.jupiter.MockitoExtension
import reactor.test.StepVerifier
import software.amazon.awssdk.core.ResponseBytes
import software.amazon.awssdk.core.async.AsyncRequestBody
import software.amazon.awssdk.core.async.AsyncResponseTransformer
import software.amazon.awssdk.http.SdkHttpResponse
import software.amazon.awssdk.services.s3.S3AsyncClient
import software.amazon.awssdk.services.s3.model.*
import java.nio.file.Path
import java.util.concurrent.CompletableFuture

@ExtendWith(MockitoExtension::class)
class AWSS3StorageGatewayUnitTest {

    @Mock
    private lateinit var s3AsyncClient: S3AsyncClient

    private lateinit var storageGateway: AWSS3StorageGateway
    private val bucketName = "test-bucket"

    @TempDir
    lateinit var tempDir: Path

    @BeforeEach
    fun setUp() {
        storageGateway = AWSS3StorageGateway(s3AsyncClient, bucketName)
    }

    @Test
    fun `upload should return true when s3 put succeeds`() {
        val file = tempDir.resolve("test.txt").toFile()
        file.writeText("content")
        val filePath = "uploads/test.txt"

        val sdkResponse = mock(SdkHttpResponse::class.java)
        `when`(sdkResponse.isSuccessful).thenReturn(true)
        val putResponse = PutObjectResponse.builder().build()
        val responseWithHttp = putResponse.toBuilder().sdkHttpResponse(sdkResponse).build() as PutObjectResponse

        `when`(
            s3AsyncClient.putObject(
                any(PutObjectRequest::class.java),
                any(AsyncRequestBody::class.java)
            )
        ).thenReturn(CompletableFuture.completedFuture(responseWithHttp))

        StepVerifier.create(storageGateway.upload(file, filePath)).expectNext(true).verifyComplete()

        verify(s3AsyncClient).putObject(any(PutObjectRequest::class.java), any(AsyncRequestBody::class.java))
    }

    @Test
    fun `upload should return false when s3 put fails with exception`() {
        val file = tempDir.resolve("test.txt").toFile()
        file.writeText("content")
        val filePath = "uploads/test.txt"

        `when`(
            s3AsyncClient.putObject(
                any(PutObjectRequest::class.java),
                any(AsyncRequestBody::class.java)
            )
        ).thenReturn(CompletableFuture.failedFuture(RuntimeException("S3 Error")))

        StepVerifier.create(storageGateway.upload(file, filePath)).expectNext(false).verifyComplete()
    }

    @Test
    fun `upload should return false when sdk response is not successful`() {
        val file = tempDir.resolve("test.txt").toFile()
        file.writeText("content")
        val filePath = "uploads/test.txt"

        val sdkResponse = mock(SdkHttpResponse::class.java)
        `when`(sdkResponse.isSuccessful).thenReturn(false)
        val putResponse = PutObjectResponse.builder().build()
        val responseWithHttp = putResponse.toBuilder().sdkHttpResponse(sdkResponse).build() as PutObjectResponse

        `when`(
            s3AsyncClient.putObject(
                any(PutObjectRequest::class.java),
                any(AsyncRequestBody::class.java)
            )
        ).thenReturn(CompletableFuture.completedFuture(responseWithHttp))

        StepVerifier.create(storageGateway.upload(file, filePath)).expectNext(false).verifyComplete()
    }

    @Test
    fun `delete should return true when s3 delete succeeds`() {
        val fileName = "test-file.txt"
        val sdkResponse = mock(SdkHttpResponse::class.java)
        `when`(sdkResponse.isSuccessful).thenReturn(true)
        val deleteResponse = DeleteObjectResponse.builder().build()
        val responseWithHttp = deleteResponse.toBuilder().sdkHttpResponse(sdkResponse).build() as DeleteObjectResponse

        `when`(s3AsyncClient.deleteObject(any(DeleteObjectRequest::class.java))).thenReturn(
                CompletableFuture.completedFuture(
                    responseWithHttp
                )
            )

        StepVerifier.create(storageGateway.delete(fileName)).expectNext(true).verifyComplete()

        verify(s3AsyncClient).deleteObject(any(DeleteObjectRequest::class.java))
    }

    @Test
    fun `delete should return false when fileName is blank`() {
        StepVerifier.create(storageGateway.delete("   ")).expectNext(false).verifyComplete()

        verifyNoInteractions(s3AsyncClient)
    }

    @Test
    fun `delete should return false when s3 delete fails with exception`() {
        val fileName = "test-file.txt"

        `when`(s3AsyncClient.deleteObject(any(DeleteObjectRequest::class.java))).thenReturn(
                CompletableFuture.failedFuture(
                    RuntimeException("Network error")
                )
            )

        StepVerifier.create(storageGateway.delete(fileName)).expectNext(false).verifyComplete()
    }

    @Test
    fun `delete should return false when sdk response is not successful`() {
        val fileName = "test-file.txt"
        val sdkResponse = mock(SdkHttpResponse::class.java)
        `when`(sdkResponse.isSuccessful).thenReturn(false)
        val deleteResponse = DeleteObjectResponse.builder().build()
        val responseWithHttp = deleteResponse.toBuilder().sdkHttpResponse(sdkResponse).build() as DeleteObjectResponse

        `when`(s3AsyncClient.deleteObject(any(DeleteObjectRequest::class.java))).thenReturn(
                CompletableFuture.completedFuture(
                    responseWithHttp
                )
            )

        StepVerifier.create(storageGateway.delete(fileName)).expectNext(false).verifyComplete()
    }

    @Test
    fun `read should return byte array when s3 get succeeds`() {
        val fileName = "test-file.txt"
        val content = "hello world".toByteArray()
        val responseBytes = ResponseBytes.fromByteArray(GetObjectResponse.builder().build(), content)

        `when`(
            s3AsyncClient.getObject(
                any(GetObjectRequest::class.java),
                any<AsyncResponseTransformer<GetObjectResponse, ResponseBytes<GetObjectResponse>>>()
            )
        ).thenReturn(CompletableFuture.completedFuture(responseBytes))

        StepVerifier.create(storageGateway.read(fileName)).consumeNextWith { result ->
                assertArrayEquals(content, result)
            }.verifyComplete()

        verify(s3AsyncClient).getObject(
            any(GetObjectRequest::class.java),
            any<AsyncResponseTransformer<GetObjectResponse, ResponseBytes<GetObjectResponse>>>()
        )
    }

    @Test
    fun `read should return empty mono when fileName is blank`() {
        StepVerifier.create(storageGateway.read("")).verifyComplete()

        verifyNoInteractions(s3AsyncClient)
    }

    @Test
    fun `read should propagate exception when s3 get fails`() {
        val fileName = "missing.txt"
        val exception = S3Exception.builder().message("Not Found").statusCode(404).build()

        `when`(
            s3AsyncClient.getObject(
                any(GetObjectRequest::class.java),
                any<AsyncResponseTransformer<GetObjectResponse, ResponseBytes<GetObjectResponse>>>()
            )
        ).thenReturn(CompletableFuture.failedFuture(exception))

        StepVerifier.create(storageGateway.read(fileName)).expectError(S3Exception::class.java).verify()
    }

    @Test
    fun `update should return true when s3 put succeeds`() {
        val fileName = "update.txt"
        val file = tempDir.resolve("update.txt").toFile()
        file.writeText("new content")

        val sdkResponse = mock(SdkHttpResponse::class.java)
        `when`(sdkResponse.isSuccessful).thenReturn(true)
        val putResponse = PutObjectResponse.builder().build()
        val responseWithHttp = putResponse.toBuilder().sdkHttpResponse(sdkResponse).build() as PutObjectResponse

        `when`(
            s3AsyncClient.putObject(
                any(PutObjectRequest::class.java),
                any(AsyncRequestBody::class.java)
            )
        ).thenReturn(CompletableFuture.completedFuture(responseWithHttp))

        StepVerifier.create(storageGateway.update(fileName, file)).expectNext(true).verifyComplete()

        verify(s3AsyncClient).putObject(any(PutObjectRequest::class.java), any(AsyncRequestBody::class.java))
    }

    @Test
    fun `update should return false when fileName is blank`() {
        val file = tempDir.resolve("any.txt").toFile()

        StepVerifier.create(storageGateway.update("", file)).expectNext(false).verifyComplete()

        verifyNoInteractions(s3AsyncClient)
    }

    @Test
    fun `update should return false when s3 put fails with exception`() {
        val fileName = "update.txt"
        val file = tempDir.resolve("update.txt").toFile()
        file.writeText("data")

        `when`(
            s3AsyncClient.putObject(
                any(PutObjectRequest::class.java),
                any(AsyncRequestBody::class.java)
            )
        ).thenReturn(CompletableFuture.failedFuture(RuntimeException("AWS Down")))

        StepVerifier.create(storageGateway.update(fileName, file)).expectNext(false).verifyComplete()
    }

    @Test
    fun `update should return false when sdk response is not successful`() {
        val fileName = "update.txt"
        val file = tempDir.resolve("update.txt").toFile()
        file.writeText("data")

        val sdkResponse = mock(SdkHttpResponse::class.java)
        `when`(sdkResponse.isSuccessful).thenReturn(false)

        val responseWithHttp = PutObjectResponse.builder().build()
            .toBuilder().sdkHttpResponse(sdkResponse).build() as PutObjectResponse

        `when`(s3AsyncClient.putObject(any(PutObjectRequest::class.java), any(AsyncRequestBody::class.java)))
            .thenReturn(CompletableFuture.completedFuture(responseWithHttp))

        StepVerifier.create(storageGateway.update(fileName, file))
            .expectNext(false)
            .verifyComplete()
    }
}
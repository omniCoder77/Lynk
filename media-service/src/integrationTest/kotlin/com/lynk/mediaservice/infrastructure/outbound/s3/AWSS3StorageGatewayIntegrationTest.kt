package com.lynk.mediaservice.infrastructure.outbound.s3

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.localstack.LocalStackContainer
import org.testcontainers.utility.DockerImageName
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.core.async.AsyncRequestBody
import software.amazon.awssdk.core.async.AsyncResponseTransformer
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3AsyncClient
import software.amazon.awssdk.services.s3.model.CreateBucketRequest
import software.amazon.awssdk.services.s3.model.HeadObjectRequest
import software.amazon.awssdk.services.s3.model.NoSuchKeyException
import java.nio.file.Path

@Testcontainers
class AWSS3StorageGatewayIntegrationTest {

    val s3AsyncClient = S3AsyncClient.builder().endpointOverride(localStack.endpoint).credentialsProvider(
        StaticCredentialsProvider.create(
            AwsBasicCredentials.create(localStack.accessKey, localStack.secretKey)
        )
    ).region(Region.of(localStack.region)).forcePathStyle(true).build()

    companion object {
        const val BUCKET_NAME = "integration-test-bucket"

        @Container
        val localStack: LocalStackContainer =
            LocalStackContainer(DockerImageName.parse("localstack/localstack:s3-latest")).withServices("s3")
    }

    private var storageGateway: AWSS3StorageGateway = AWSS3StorageGateway(s3AsyncClient, BUCKET_NAME)

    @TempDir
    lateinit var tempDir: Path

    @BeforeEach
    fun setUp() {
        s3AsyncClient.createBucket(CreateBucketRequest.builder().bucket(BUCKET_NAME).build()).join()
    }

    @Test
    fun `should upload file to S3 (LocalStack)`() {
        val file = tempDir.resolve("hello.txt").toFile()
        file.writeText("Hello Integration World")

        StepVerifier.create(storageGateway.upload(file)).expectNext(true).verifyComplete()

        val response = s3AsyncClient.headObject(
            HeadObjectRequest.builder().bucket(BUCKET_NAME).key("hello.txt").build()
        ).join()

        assert(response.sdkHttpResponse().isSuccessful)
    }

    @Test
    fun `should read file from S3 (LocalStack)`() {
        val key = "read-me.txt"
        val content = "Content from S3"
        s3AsyncClient.putObject(
            { builder -> builder.bucket(BUCKET_NAME).key(key) }, AsyncRequestBody.fromString(content)
        ).join()

        StepVerifier.create(storageGateway.read(key)).consumeNextWith { bytes ->
            val result = String(bytes)
            assert(result == content) { "Expected '$content' but got '$result'" }
        }.verifyComplete()
    }

    @Test
    fun `should return false while reading when fileName is empty`() {
        val name = ""
        StepVerifier.create(storageGateway.read(name)).expectNext().verifyComplete()
    }

    @Test
    fun `should delete file from S3 (LocalStack)`() {
        val key = "delete-me.txt"
        val file = tempDir.resolve(key).toFile()
        file.writeText("Delete me")

        s3AsyncClient.putObject(
            { b -> b.bucket(BUCKET_NAME).key(key) }, AsyncRequestBody.fromFile(file)
        ).join()

        StepVerifier.create(storageGateway.delete(key)).expectNext(true).verifyComplete()

        StepVerifier.create(
            Mono.fromFuture(
                s3AsyncClient.headObject(HeadObjectRequest.builder().bucket(BUCKET_NAME).key(key).build())
            )
        ).expectError(NoSuchKeyException::class.java).verify()
    }

    @Test
    fun `should return false when fileName is empty`() {
        val fileName = ""
        StepVerifier.create(storageGateway.delete(fileName)).expectNext(false).verifyComplete()
    }

    @Test
    fun `should update existing file in S3 (LocalStack)`() {
        val key = "update.txt"
        s3AsyncClient.putObject(
            { b -> b.bucket(BUCKET_NAME).key(key) }, AsyncRequestBody.fromString("Original Content")
        ).join()

        val newFile = tempDir.resolve("new-version.txt").toFile()
        newFile.writeText("Updated Content")

        StepVerifier.create(storageGateway.update(key, newFile)).expectNext(true).verifyComplete()

        val actualBytes = s3AsyncClient.getObject(
            { b -> b.bucket(BUCKET_NAME).key(key) }, AsyncResponseTransformer.toBytes()
        ).join().asByteArray()

        assert(String(actualBytes) == "Updated Content")
    }

    @Test
    fun `should return false when updating empty fileName`() {
        StepVerifier.create(storageGateway.update("", tempDir.resolve("any.txt").toFile())).expectNext(false)
            .verifyComplete()
    }
}
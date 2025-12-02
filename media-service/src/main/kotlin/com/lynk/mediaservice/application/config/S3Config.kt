package com.lynk.mediaservice.application.config

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3AsyncClient
import software.amazon.awssdk.services.s3.model.CreateBucketRequest
import software.amazon.awssdk.services.s3.model.HeadBucketRequest
import software.amazon.awssdk.services.s3.model.S3Exception
import java.util.concurrent.CompletionException

@Configuration
class S3Config {

    private val logger = LoggerFactory.getLogger(this::class.java)

    @Bean
    fun s3Client(
        @Value("\${aws.access.key}") accessKey: String,
        @Value("\${aws.secret.key}") secretKey: String,
        @Value("\${aws.s3.bucket.name}") s3Bucket: String,
    ): S3AsyncClient {
        val client = S3AsyncClient.builder().region(Region.US_EAST_1).credentialsProvider(
            StaticCredentialsProvider.create(
                AwsBasicCredentials.create(accessKey, secretKey)
            )
        ).build()

        try {
            val future = client.headBucket(
                HeadBucketRequest.builder().bucket(s3Bucket).build()
            )
            val response = future.join()

            if (response.sdkHttpResponse().statusCode() == 200) {
                logger.info("S3 bucket '$s3Bucket' exists and is accessible")
            }
        } catch (e: CompletionException) {
            val cause = e.cause
            if (cause is S3Exception) {
                when (cause.statusCode()) {
                    404 -> {
                        logger.info("Bucket '$s3Bucket' not found, creating it...")
                        client.createBucket(CreateBucketRequest.builder().bucket(s3Bucket).build()).join()
                        logger.info("Bucket '$s3Bucket' created successfully")
                    }

                    403 -> throw RuntimeException("Access to the bucket '$s3Bucket' is forbidden", cause)
                    else -> throw RuntimeException("Error accessing bucket '$s3Bucket'", cause)
                }
            } else {
                throw RuntimeException("Unexpected error", cause)
            }
        }

        return client
    }
}
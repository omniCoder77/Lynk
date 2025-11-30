import org.gradle.kotlin.dsl.implementation

plugins {
    kotlin("jvm") version "2.1.21"
    kotlin("plugin.spring") version "2.1.21"
    id("org.springframework.boot") version "3.5.4"
    id("io.spring.dependency-management") version "1.1.7"
}

group = "com.lynk"
version = "0.0.1-SNAPSHOT"
description = "message-service"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
}

extra["testcontainers.version"] = "2.0.2"

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-data-cassandra-reactive")
    implementation("org.springframework.boot:spring-boot-starter-data-redis-reactive")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("io.projectreactor.kotlin:reactor-kotlin-extensions")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")
    implementation("org.springframework.kafka:spring-kafka")
    implementation("org.springframework.boot:spring-boot-starter-cache")


    testImplementation(platform("org.testcontainers:testcontainers-bom:2.0.2"))

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.boot:spring-boot-testcontainers")

    testImplementation("org.testcontainers:testcontainers-junit-jupiter")
    testImplementation("org.testcontainers:testcontainers-kafka")
    testImplementation("org.testcontainers:testcontainers-cassandra")

    testImplementation("com.redis:testcontainers-redis:2.2.2")
    testImplementation("io.projectreactor:reactor-test:3.8.0")

    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    implementation("org.mockito.kotlin:mockito-kotlin:5.4.0")

    implementation("com.razorpay:razorpay-java:1.4.8")
    implementation("io.jsonwebtoken:jjwt-api:0.12.6")
    implementation("io.jsonwebtoken:jjwt-impl:0.12.6")
    implementation("io.jsonwebtoken:jjwt-jackson:0.12.6")

    implementation("org.webjars:webjars-locator-core")
    implementation("org.webjars:sockjs-client:1.5.1")
    implementation("org.webjars:stomp-websocket:2.3.4")

    implementation("org.springframework:spring-messaging")
    implementation("org.springframework:spring-websocket")

    implementation("redis.clients:jedis:6.0.0")

    implementation("org.springdoc:springdoc-openapi-starter-webflux-ui:2.5.0")

    developmentOnly("org.springframework.boot:spring-boot-devtools")

    implementation("jakarta.validation:jakarta.validation-api:3.1.1")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")
    implementation("com.google.firebase:firebase-admin:9.7.0")
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict")
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

dependencyLocking {
    lockAllConfigurations()
}
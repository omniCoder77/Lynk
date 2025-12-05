plugins {
    kotlin("jvm") version "1.9.25"
    kotlin("plugin.spring") version "1.9.25"
    id("org.springframework.boot") version "3.5.7"
    id("io.spring.dependency-management") version "1.1.7"
    id("com.google.protobuf") version "0.9.5"
}

group = "com.ethyllium"
version = "0.0.1-SNAPSHOT"
description = "user-service"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
}

extra["testcontainers.version"] = "2.0.2"
extra["springCloudVersion"] = "2024.0.1"
val springGrpcVersion by extra("0.8.0")
val kotlinStubVersion = "1.4.3"

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-data-redis-reactive")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("io.projectreactor.kotlin:reactor-kotlin-extensions")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.18.2")
    implementation("org.postgresql:postgresql")
    implementation("org.springframework.boot:spring-boot-starter-data-r2dbc")
    implementation("org.postgresql:r2dbc-postgresql")
    implementation("org.springframework.kafka:spring-kafka")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test")
    testImplementation("org.springframework.kafka:spring-kafka-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    implementation("org.springframework.grpc:spring-grpc-spring-boot-starter")
    implementation("io.grpc:grpc-services")
    implementation("io.grpc:grpc-kotlin-stub:${kotlinStubVersion}")

    testImplementation("org.springframework.boot:spring-boot-testcontainers")
    implementation("io.mockk:mockk:1.14.3")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation(platform("org.testcontainers:testcontainers-bom:2.0.2"))
    testImplementation("org.testcontainers:testcontainers-junit-jupiter")
    testImplementation("org.testcontainers:testcontainers-kafka")
    testImplementation("com.redis:testcontainers-redis")
    testImplementation("io.projectreactor:reactor-test:3.8.0")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.cloud:spring-cloud-dependencies:${property("springCloudVersion")}")
        mavenBom("org.springframework.grpc:spring-grpc-dependencies:$springGrpcVersion")
    }
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict", "-Xannotation-default-target=param-property")
    }
}

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:${dependencyManagement.importedProperties["protobuf-java.version"]}"
    }
    plugins {
        create("grpc") {
            artifact = "io.grpc:protoc-gen-grpc-java:${dependencyManagement.importedProperties["grpc.version"]}"
        }
        create("grpckt") {
            artifact = "io.grpc:protoc-gen-grpc-kotlin:${kotlinStubVersion}:jdk8@jar"
        }
    }
    generateProtoTasks {
        all().forEach { task ->
            task.plugins {
                create("grpc") {
                    option("jakarta_omit")
                    option("@generated=omit")
                }
                create("grpckt") {
                    outputSubDir = "kotlin"
                }
            }
        }
    }
}

val integrationTest by sourceSets.creating {
    kotlin.srcDir("src/integrationTest/kotlin")
    resources.srcDir("src/integrationTest/resources")

    compileClasspath += sourceSets["main"].output + sourceSets["test"].output + configurations["testCompileClasspath"]
    runtimeClasspath += sourceSets["main"].output + sourceSets["test"].output + configurations["testRuntimeClasspath"]
}

val e2eTest by sourceSets.creating {
    kotlin.srcDir("src/e2eTest/kotlin")
    resources.srcDir("src/e2eTest/resources")

    compileClasspath += sourceSets["main"].output + sourceSets["test"].output + configurations["testCompileClasspath"]
    runtimeClasspath += sourceSets["main"].output + sourceSets["test"].output + configurations["testRuntimeClasspath"]
}


tasks.register<Test>("integrationTest") {
    testClassesDirs = integrationTest.output.classesDirs
    classpath = integrationTest.runtimeClasspath
    useJUnitPlatform()
}

tasks.register<Test>("e2eTest") {
    testClassesDirs = e2eTest.output.classesDirs
    classpath = e2eTest.runtimeClasspath
    useJUnitPlatform()
}

tasks.register("allTests") {
    dependsOn("test", "integrationTest", "e2eTest")
    doLast {
        println("All tests completed")
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

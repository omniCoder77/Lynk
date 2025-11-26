package com.lynk.mediaservice.domain.port.driver

import reactor.core.publisher.Mono
import java.io.File

interface StorageGateway {
    fun upload(file: File): Mono<Boolean>
    fun delete(fileName: String): Mono<Boolean>
    fun read(fileName: String): Mono<ByteArray>
    fun update(fileName: String, file: File): Mono<Boolean>
}

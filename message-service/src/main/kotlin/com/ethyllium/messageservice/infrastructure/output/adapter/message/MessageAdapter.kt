package com.ethyllium.messageservice.infrastructure.outpout.adapter.message

import com.ethyllium.messageservice.domain.model.Message
import com.ethyllium.messageservice.domain.port.MessagePort
import org.springframework.data.cassandra.core.ReactiveCassandraTemplate
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers

@Component
class MessageDatabaseAdapter(
    private val cassandraTemplate: ReactiveCassandraTemplate
) : MessagePort {

    override fun save(message: Message): Mono<Message> {
        return cassandraTemplate.insert(message).subscribeOn(Schedulers.boundedElastic())
            .onErrorMap { e -> RuntimeException("Failed to save message: ${e.message}", e) }
    }

    override fun findById(id: String): Mono<Message> {
        return cassandraTemplate.selectOneById(
            mapOf("id" to id), Message::class.java
        ).subscribeOn(Schedulers.boundedElastic())
            .onErrorMap { e -> RuntimeException("Failed to find message by ID: ${e.message}", e) }
    }

    override fun findAll(): Flux<Message> {
        return cassandraTemplate.select("SELECT * FROM messages", Message::class.java)
            .subscribeOn(Schedulers.boundedElastic())
            .onErrorMap { e -> RuntimeException("Failed to fetch all messages: ${e.message}", e) }
    }
}
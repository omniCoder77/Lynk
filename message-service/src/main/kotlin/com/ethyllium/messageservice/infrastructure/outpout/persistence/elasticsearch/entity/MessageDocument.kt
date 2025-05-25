package com.ethyllium.messageservice.infrastructure.outpout.persistence.elasticsearch.entity

import org.springframework.data.annotation.Id
import org.springframework.data.elasticsearch.annotations.*
import java.time.Instant
import java.util.*

@Document(indexName = "messages")
data class MessageDocument(

    @Id
    val id: String? = null, // UUID string format conversationId_messageId

    @Field(type = FieldType.Text, analyzer = "standard", searchAnalyzer = "standard")
    val content: String,

    @Field(type = FieldType.Date)
    val timestamp: Instant = Instant.now(),

    @Field(type = FieldType.Keyword)
    val conversationId: UUID,

    @Field(type = FieldType.Keyword)
    val senderId: UUID,

    @Field(type = FieldType.Keyword)
    val receiverId: UUID,

    @Field(type = FieldType.Keyword)
    val status: String = "SENT"
)

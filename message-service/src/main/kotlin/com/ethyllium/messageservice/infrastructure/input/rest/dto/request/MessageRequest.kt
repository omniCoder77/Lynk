package com.ethyllium.messageservice.infrastructure.input.resp.dto.request

data class MessageRequest(
    val content: String,
    val sender: String,
)
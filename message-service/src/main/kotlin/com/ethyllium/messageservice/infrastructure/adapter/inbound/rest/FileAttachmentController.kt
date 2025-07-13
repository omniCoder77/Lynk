package com.ethyllium.messageservice.infrastructure.adapter.inbound.rest

import com.ethyllium.messageservice.application.dto.FileAttachmentResponse
import com.ethyllium.messageservice.application.port.outbound.FileAttachmentService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/api/files")
class FileAttachmentController(
    private val fileAttachmentService: FileAttachmentService
) {
    @PostMapping("/upload")
    fun uploadFile(
        @RequestHeader("X-User-Id") userId: String,
        @RequestPart("file") file: MultipartFile,
        @RequestParam conversationId: String,
        @RequestParam(required = false) messageReference: String?
    ): ResponseEntity<FileAttachmentResponse> {
        val response = fileAttachmentService.uploadFile(userId, file, conversationId, messageReference)
        return ResponseEntity.ok(response)
    }

    @GetMapping("/{fileId}")
    fun getFile(
        @RequestHeader("X-User-Id") userId: String,
        @PathVariable fileId: String,
        @RequestParam conversationId: String,
        @RequestParam(required = false) messageReference: String?
    ): ResponseEntity<FileAttachmentResponse> {
        val response = fileAttachmentService.getFile(userId, fileId, conversationId, messageReference)
        return ResponseEntity.ok(response)
    }

    @DeleteMapping("/{fileId}")
    fun deleteFile(
        @RequestHeader("X-User-Id") userId: String, @PathVariable fileId: String
    ): ResponseEntity<Void> {
        fileAttachmentService.deleteFile(userId, fileId)
        return ResponseEntity.noContent().build()
    }
}

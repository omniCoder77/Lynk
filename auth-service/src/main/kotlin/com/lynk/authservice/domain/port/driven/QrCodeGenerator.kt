package com.lynk.authservice.domain.port.driven

interface QrCodeGenerator {
    fun generateQrCode(uri: String, width: Int, height: Int): ByteArray
}
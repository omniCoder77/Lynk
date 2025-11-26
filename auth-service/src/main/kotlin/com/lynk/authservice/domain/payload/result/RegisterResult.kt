package com.lynk.authservice.domain.payload.result

sealed interface RegisterResult {
    data object SmsSendFailed : RegisterResult
    data object FirstNameEmpty: RegisterResult
    data object Success: RegisterResult
    data object PhoneNumberEmpty: RegisterResult
    data object InvalidPhoneNumber: RegisterResult
    data object LastNameEmpty: RegisterResult
    data class MFAQrCode(val qrCode: ByteArray): RegisterResult
}
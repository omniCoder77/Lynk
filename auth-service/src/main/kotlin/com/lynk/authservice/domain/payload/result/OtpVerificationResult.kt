package com.lynk.authservice.domain.payload.result

sealed interface OtpVerificationResult {
    data object InvalidOtp: OtpVerificationResult
    data object Success: OtpVerificationResult
    data object ExpiredOTP: OtpVerificationResult
}
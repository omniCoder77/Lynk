package com.lynk.authservice.infrastructure.inbound.web.dto

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.annotation.JsonTypeName

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type"
)
@JsonSubTypes(
    JsonSubTypes.Type(value = RegisterResponse.OTP::class, name = "otp"),
    JsonSubTypes.Type(value = RegisterResponse.MFA::class, name = "mfa"),
    JsonSubTypes.Type(value = RegisterResponse.Token::class, name = "token")
)
sealed interface RegisterResponse {

    @JsonTypeName("otp")
    data object OTP : RegisterResponse

    @JsonTypeName("mfa")
    data class MFA(
        val qrCode: String
    ) : RegisterResponse

    @JsonTypeName("token")
    data class Token(val accessToken: String, val refreshToken: String) : RegisterResponse
}
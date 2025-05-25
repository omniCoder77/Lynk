package com.ethyllium.authservice.domain.model

data class User(
    val name: String,
    val phoneNumber: String,
    val mfaType: MFAType = MFAType.NONE,
    val mfaToken: String? = null,
    val userId: String,
    val password: String
)
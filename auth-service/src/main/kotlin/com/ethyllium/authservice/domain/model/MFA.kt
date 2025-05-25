package com.ethyllium.authservice.domain.model

enum class MFAType {
    NONE, AUTHENTICATOR, SECURITY_CODE;

    companion object {
        fun get(mfaType: String): MFAType {
            return when (mfaType) {
                NONE.name -> NONE
                AUTHENTICATOR.name -> return AUTHENTICATOR
                SECURITY_CODE.name -> return SECURITY_CODE
                else -> throw IllegalArgumentException("Unsupported mFAType $mfaType")
            }
        }
    }
}
package com.ethyllium.authservice.infrastructure.adapter.output.persistence.entity

import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.annotation.Version
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.OffsetDateTime

@Table("users")
data class UserEntity(
    @Column("user_id") val userId: String,

    @Column("name") val name: String,

    @Column("phone_number") val phoneNumber: String,

    @Column("mfa_type") val mfaType: String,

    @Column("mfa_token") val mfaToken: String? = null,

    @Column("is_mfa_enabled") val isMfaEnabled: Boolean = false,

    @Column("password") val password: String,

    @Version @Column("version") val version: Int = 0,

    @CreatedDate @Column("created_at") val createdAt: OffsetDateTime = OffsetDateTime.now(),

    @LastModifiedDate @Column("updated_at") val updatedAt: OffsetDateTime = createdAt
)
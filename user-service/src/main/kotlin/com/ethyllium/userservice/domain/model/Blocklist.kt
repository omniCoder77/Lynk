package com.ethyllium.userservice.domain.model

import java.util.*

data class Blocklist(
    val blocklistId: UUID, val userId: UUID, val blockedUserId: UUID
)
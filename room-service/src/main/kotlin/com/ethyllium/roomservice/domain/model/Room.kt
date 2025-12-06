package com.ethyllium.roomservice.domain.model

import java.util.*

data class Room(val roomId: UUID, val name: String, val maxSize: Int, val visibility: Visibility)

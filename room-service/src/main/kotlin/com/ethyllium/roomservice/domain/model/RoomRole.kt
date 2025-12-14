package com.ethyllium.roomservice.domain.model

enum class RoomRole(val priority: Int) {
    ADMIN(2),
    MODERATOR(1),
    MEMBER(0),
    NON_MEMBER(-1)
}
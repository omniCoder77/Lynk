package com.ethyllium.roomservice.domain.exception

class UnauthorizedRoomActionException(override val message: String = "User is not authorized to perform this action on the room"): Exception(message)
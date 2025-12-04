package com.lynk.mediaservice.domain.exception

class UnauthorizedRoomActionException(action: String):
    RuntimeException("Action $action cannot be performed on the room due to lack of permissions")
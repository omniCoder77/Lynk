package com.ethyllium.authservice.infrastructure.adapter.output.persistence.mapper

import com.ethyllium.authservice.domain.model.MFAType
import com.ethyllium.authservice.domain.model.User
import com.ethyllium.authservice.infrastructure.adapter.output.persistence.entity.UserEntity

class UserMapper {
    companion object {
        fun toUser(userEntity: UserEntity) = User(
            name = userEntity.name,
            phoneNumber = userEntity.phoneNumber,
            mfaType = MFAType.get(userEntity.mfaType),
            mfaToken = userEntity.mfaToken,
            userId = userEntity.userId,
            password = userEntity.password
        )

        fun toUserEntity(user: User, password: String) = UserEntity(
            userId = user.userId,
            name = user.name,
            phoneNumber = user.phoneNumber,
            mfaType = user.mfaType.name,
            mfaToken = user.mfaToken,
            password = password
        )
    }
}
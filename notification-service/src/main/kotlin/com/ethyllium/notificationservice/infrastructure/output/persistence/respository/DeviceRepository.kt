package com.ethyllium.notificationservice.infrastructure.output.persistence.respository

import com.ethyllium.notificationservice.infrastructure.output.persistence.entity.Device
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.r2dbc.repository.R2dbcRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono

@Repository
interface DeviceRepository: R2dbcRepository<Device, String> {

    @Query("update device set token = :token where device_id = :deviceId")
    fun updateToken(token: String, deviceId: String): Mono<Int>
}
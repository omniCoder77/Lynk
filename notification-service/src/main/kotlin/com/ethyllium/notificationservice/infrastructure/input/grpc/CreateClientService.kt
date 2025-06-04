package com.ethyllium.notificationservice.infrastructure.input.grpc

import com.ethyllium.notificationservice.infrastructure.output.persistence.respository.DeviceRepository
import io.grpc.stub.StreamObserver
import net.devh.boot.grpc.server.service.GrpcService

@GrpcService
class CreateClientService(
    private val deviceRepository: DeviceRepository
) : ClientTokenGrpc.ClientTokenImplBase() {
    override fun createClient(
        request: CreateDeviceRequest, responseObserver: StreamObserver<CreateDeviceResponse>
    ) {
        super.createClient(request, responseObserver)
        deviceRepository.findById(request.userId).subscribe({ device ->
            deviceRepository.updateToken(request.token, request.userId).subscribe({
                val res = CreateDeviceResponse.newBuilder().setCreated(true).build()
                responseObserver.onNext(res)
                responseObserver.onCompleted()
            }, { error ->
                responseObserver.onError(error)
            })
        }, { error ->
            responseObserver.onError(error)
            responseObserver.onCompleted()
        })
    }
}
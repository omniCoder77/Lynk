package com.ethyllium.authservice.infrastructure.config

import io.grpc.Metadata
import io.grpc.ServerCall
import io.grpc.ServerCallHandler
import io.grpc.ServerInterceptor
import org.slf4j.LoggerFactory

class LoggingServerInterceptor : ServerInterceptor {
    private val logger = LoggerFactory.getLogger(this::class.java)
    override fun <ReqT : Any, RespT : Any> interceptCall(
        call: ServerCall<ReqT, RespT>, headers: Metadata, next: ServerCallHandler<ReqT, RespT>
    ): ServerCall.Listener<ReqT?>? {
        logger.info("gRPC call: ${call.methodDescriptor.fullMethodName}")
        println("gRPC call: ${call.methodDescriptor.fullMethodName}")
        return next.startCall(call, headers)
    }
}
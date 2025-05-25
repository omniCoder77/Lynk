package com.ethyllium.authservice.infrastructure.adapter.input.grpc

import com.ethyllium.authservice.domain.service.LoginService
import com.ethyllium.authservice.infrastructure.adapter.input.grpc.dto.LoginResult
import io.grpc.stub.StreamObserver
import org.springframework.grpc.server.service.GrpcService

@GrpcService
class LoginGrpcService(private val loginService: LoginService) : LoginServiceGrpc.LoginServiceImplBase() {
    override fun login(
        request: LoginRequest, responseObserver: StreamObserver<LoginResponse>
    ) {
        super.login(request, responseObserver)
        loginService.login(request.phoneNumber, request.password).subscribe({
            val res = when (it) {
                is LoginResult.MfaRequired -> {
                    val mfaChallenge = MfaChallenge.newBuilder().setSessionId(it.userId).build()
                    LoginResponse.newBuilder().setMfaRequired(mfaChallenge).build()
                }

                is LoginResult.Token -> {
                    val authToken =
                        AuthTokens.newBuilder().setAccessToken(it.accessToken).setRefreshToken(it.refreshToken)
                            .setExpiresIn(it.tokenExpiration).build()
                    LoginResponse.newBuilder().setSuccess(authToken).build()
                }
            }
            responseObserver.onNext(res)
            responseObserver.onCompleted()
        }, {
            responseObserver.onError(it)
        })
    }

    override fun verifyMfa(
        request: VerifyMfaRequest, responseObserver: StreamObserver<VerifyMfaResponse>
    ) {
        super.verifyMfa(request, responseObserver)
        loginService.verify(request.sessionId, request.mfaSecret).subscribe({
            val authToken =
                AuthTokens.newBuilder().setAccessToken(it.accessToken).setRefreshToken(it.refreshToken).build()
            val res = VerifyMfaResponse.newBuilder().setTokens(authToken).build()
            responseObserver.onNext(res)
            responseObserver.onCompleted()
        }, {
            responseObserver.onError(it)
        }, {
            loginService.clearAllSession(request.sessionId)
        })
    }
}
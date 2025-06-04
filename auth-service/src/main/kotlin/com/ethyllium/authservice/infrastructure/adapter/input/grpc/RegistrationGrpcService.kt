package com.ethyllium.authservice.infrastructure.adapter.input.grpc

import com.ethyllium.authservice.domain.model.MFAType
import com.ethyllium.authservice.domain.service.RegistrationService
import com.ethyllium.authservice.domain.service.TokenService
import io.grpc.Status
import io.grpc.stub.StreamObserver
import org.slf4j.LoggerFactory
import org.springframework.grpc.server.service.GrpcService
import reactor.core.scheduler.Schedulers

@GrpcService
class RegistrationGrpcService(
    private val registrationService: RegistrationService,
    private val tokenService: TokenService
) : RegistrationServiceGrpc.RegistrationServiceImplBase() {

    private val logger = LoggerFactory.getLogger(this::class.java)

    override fun initiateRegistration(
        request: InitiateRegistrationRequest, responseObserver: StreamObserver<InitiateRegistrationResponse>
    ) {
        logger.info("Received initiate registration request for phone: ${request.phoneNumber}")
//        try {
//            registrationService.initiateRegistration(request.name, request.phoneNumber).map { sessionId ->
//                InitiateRegistrationResponse.newBuilder().setSuccess(true)
//                    .setMessage("OTP sent successfully to ${request.phoneNumber}").setSessionId(sessionId).build()
//            }.subscribeOn(Schedulers.boundedElastic()).subscribe({ response ->
//                responseObserver.onNext(response)
//            }, { error ->
//                logger.error("Error during initiate registration", error)
//                responseObserver.onError(
//                    Status.INTERNAL.withDescription("Failed to initiate registration: ${error.message}")
//                        .asRuntimeException()
//                )
//            })
//        } catch (e: Exception) {
//            responseObserver.onError(e)
//        }
        responseObserver.onCompleted()
    }

    override fun verifyPhoneNumber(
        request: VerifyPhoneNumberRequest, responseObserver: StreamObserver<VerifyPhoneNumberResponse>
    ) {
        logger.info("Received verify phone number request for phone: ${request.phoneNumber}")

        try {
            registrationService.verifyPhoneNumber(
                phoneNumber = request.phoneNumber, sessionId = request.sessionId, otp = request.otpCode
            ).subscribe { verified ->
                val res = VerifyPhoneNumberResponse.newBuilder().setVerified(verified)
                    .setMessage(if (verified) "Phone number verified successfully" else "Failed to update verification status")
                    .setSessionId(request.sessionId).build()
                responseObserver.onNext(res)
                responseObserver.onCompleted()
            }
        } catch (e: Exception) {
            logger.error("Error during verifyPhoneNumber", e)
            responseObserver.onError(e)
        }
    }

    override fun completeRegistration(
        request: CompleteRegistrationRequest, responseObserver: StreamObserver<CompleteRegistrationResponse>
    ) {
        logger.info("Received complete registration request for session: ${request.sessionId}")

        val mfaType = when (request.preferredMfaType) {
            MFATypeProto.AUTHENTICATOR -> MFAType.AUTHENTICATOR
            MFATypeProto.SECURITY_CODE -> MFAType.SECURITY_CODE
            else -> MFAType.NONE
        }

        registrationService.completeRegistration(
            sessionId = request.sessionId,
            password = request.password,
            setupMfa = request.setupMfa,
            preferredMfaType = mfaType
        ).map { result ->
            val responseBuilder = CompleteRegistrationResponse.newBuilder().setSuccess(true)
                .setMessage("Registration completed successfully").setUserId(result.userId)

            result.totpSetupData?.let { setupData ->
                val totpSetupProto = TOTPSetupDataProto.newBuilder().setSecretKey(setupData.secretKey)
                    .setQrCodeImageUri(setupData.qrCodeImageUri).setIssuer(setupData.issuer)
                    .setUsername(setupData.username).build()
                responseBuilder.setTotpSetupData(totpSetupProto)
            }
            val accessToken = tokenService.generateAccessToken(subject = result.userId)
            val refreshToken = tokenService.generateRefreshToken(subject = result.userId)
            responseBuilder.setAccessToken(accessToken).setRefreshToken(refreshToken)
            responseBuilder.build()
        }.subscribeOn(Schedulers.boundedElastic()).subscribe({ response ->
            responseObserver.onNext(response)
            responseObserver.onCompleted()
        }, { error ->
            logger.error("Error during registration completion", error)
            responseObserver.onError(
                Status.INTERNAL.withDescription("Failed to complete registration: ${error.message}")
                    .asRuntimeException()
            )
        })
    }
}
package com.lynk.messageservice.infrastructure.inbound.web.controller

import com.lynk.messageservice.domain.exception.InvalidJwtException
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode
import org.springframework.http.ProblemDetail
import org.springframework.http.ResponseEntity
import org.springframework.web.ErrorResponse
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler

@ControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(InvalidJwtException::class)
    fun handleInvalidJwtException(e: InvalidJwtException): ResponseEntity<ErrorResponse> {
        return ResponseEntity.internalServerError().body(object : ErrorResponse {
            override fun getStatusCode(): HttpStatusCode {
                return HttpStatus.INTERNAL_SERVER_ERROR
            }

            override fun getBody(): ProblemDetail {
                return ProblemDetail.forStatusAndDetail(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Secret in Jwt is invalid. Please refresh the access token by refresh token"
                )
            }
        })
    }
}
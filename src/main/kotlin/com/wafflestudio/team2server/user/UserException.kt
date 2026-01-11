package com.wafflestudio.team2server.user

import com.wafflestudio.team2server.DomainException
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode

sealed class UserException(
    errorCode: Int,
    httpStatusCode: HttpStatusCode,
    msg: String,
    cause: Throwable? = null,
) : DomainException(errorCode, httpStatusCode, msg, cause)

class SignUpLocalIdConflictException :
    UserException(
        errorCode = 0,
        httpStatusCode = HttpStatus.CONFLICT,
        msg = "User id conflict",
    )

class SignUpBadLocalIdException :
    UserException(
        errorCode = 0,
        httpStatusCode = HttpStatus.BAD_REQUEST,
        msg = "Bad user id",
    )

class SignUpBadPasswordException :
    UserException(
        errorCode = 0,
        httpStatusCode = HttpStatus.BAD_REQUEST,
        msg = "Bad password",
    )

class AuthenticateException :
    UserException(
        errorCode = 0,
        httpStatusCode = HttpStatus.UNAUTHORIZED,
        msg = "Authenticate failed",
    )

class ChangePasswordIllegalStateException :
    UserException(
        errorCode = 0,
        httpStatusCode = HttpStatus.BAD_REQUEST,
        msg = "Only local user is allowed to change password",
    )

class InvalidOldPasswordException :
    UserException(
        errorCode = 0,
        httpStatusCode = HttpStatus.BAD_REQUEST,
        msg = "Old password is invalid",
    )

class InvalidNewPasswordException :
    UserException(
        errorCode = 0,
        httpStatusCode = HttpStatus.BAD_REQUEST,
        msg = "New password is invalid",
    )

package com.wafflestudio.team2server.email

import com.wafflestudio.team2server.DomainException
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode

sealed class EmailException(
    errorCode: Int,
    httpStatusCode: HttpStatusCode,
    msg: String,
    cause: Throwable? = null,
) : DomainException(errorCode, httpStatusCode, msg, cause)

class EmailAlreadyExistsException :
    EmailException(
        errorCode = 0,
        httpStatusCode = HttpStatus.CONFLICT,
        msg = "이미 등록된 이메일입니다.",
    )

class EmailNotFoundException :
    EmailException(
        errorCode = 0,
        httpStatusCode = HttpStatus.NOT_FOUND,
        msg = "해당 이메일이 존재하지 않거나 본인의 이메일이 아닙니다.",
    )

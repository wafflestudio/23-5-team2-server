package com.wafflestudio.team2server.board

import com.wafflestudio.team2server.DomainException
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode

sealed class BoardException(
    errorCode: Int,
    httpStatusCode: HttpStatusCode,
    msg: String,
    cause: Throwable? = null,
) : DomainException(errorCode, httpStatusCode, msg, cause)

class BoardNotFoundException :
    BoardException(
        errorCode = 0,
        httpStatusCode = HttpStatus.NOT_FOUND,
        msg = "Board not found",
    )

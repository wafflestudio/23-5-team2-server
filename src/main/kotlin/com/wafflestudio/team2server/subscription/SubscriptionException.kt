package com.wafflestudio.team2server.subscription

import com.wafflestudio.team2server.DomainException
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode

sealed class SubscriptionException(
    errorCode: Int,
    httpStatusCode: HttpStatusCode,
    msg: String,
    cause: Throwable? = null,
) : DomainException(errorCode, httpStatusCode, msg, cause)

class DuplicateSubscriptionException :
    SubscriptionException(
        errorCode = 0,
        httpStatusCode = HttpStatus.CONFLICT,
        msg = "The subscription already exists.",
    )

class SubscriptionNotFoundException :
    SubscriptionException(
        errorCode = 0,
        httpStatusCode = HttpStatus.NOT_FOUND,
        msg = "The subscription is not found.",
    )

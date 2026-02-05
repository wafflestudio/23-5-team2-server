package com.wafflestudio.team2server.article

import com.wafflestudio.team2server.DomainException
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode

sealed class ArticleException(
    errorCode: Int,
    httpStatusCode: HttpStatusCode,
    msg: String,
    cause: Throwable? = null,
) : DomainException(errorCode, httpStatusCode, msg, cause)

class ArticleNotFoundException :
    ArticleException(
        errorCode = 0,
        httpStatusCode = HttpStatus.NOT_FOUND,
        msg = "Article not found",
    )

class ArticleBlankContentException :
    ArticleException(
        errorCode = 0,
        httpStatusCode = HttpStatus.BAD_REQUEST,
        msg = "Content must not be blank",
    )

class ArticleBlankAuthorException :
    ArticleException(
        errorCode = 0,
        httpStatusCode = HttpStatus.BAD_REQUEST,
        msg = "Author must not be blank",
    )

class ArticleBlankPublishedException :
    ArticleException(
        errorCode = 0,
        httpStatusCode = HttpStatus.BAD_REQUEST,
        msg = "PublishedAt must not be blank",
    )

class ArticleBlankOriginLinkException :
    ArticleException(
        errorCode = 0,
        httpStatusCode = HttpStatus.BAD_REQUEST,
        msg = "OriginLink must not be blank",
    )

class ArticleBlankTitleException :
    ArticleException(
        errorCode = 0,
        httpStatusCode = HttpStatus.BAD_REQUEST,
        msg = "Title must not be blank",
    )

class StandardNotFoundException :
    ArticleException(
        errorCode = 0,
        httpStatusCode = HttpStatus.BAD_REQUEST,
        msg = "hotStandard not found(by id = 1L)",
    )

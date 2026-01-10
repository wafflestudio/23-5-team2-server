package com.wafflestudio.team2server.image

import com.wafflestudio.team2server.DomainException
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode

sealed class ImageException(
    errorCode: Int,
    httpStatusCode: HttpStatusCode,
    msg: String,
    cause: Throwable? = null,
) : DomainException(errorCode, httpStatusCode, msg, cause)

class InvalidFilenameException :
    ImageException(
        errorCode = 0,
        httpStatusCode = HttpStatus.BAD_REQUEST,
        msg = "Filename is null",
    )

class InvalidExtensionException :
    ImageException(
        errorCode = 0,
        httpStatusCode = HttpStatus.BAD_REQUEST,
        msg = "The extension is not allowed",
    )

class InvalidMimeTypeException :
    ImageException(
        errorCode = 0,
        httpStatusCode = HttpStatus.BAD_REQUEST,
        msg = "The mime type is not allowed",
    )

class InvalidFileSizeException :
    ImageException(
        errorCode = 0,
        httpStatusCode = HttpStatus.PAYLOAD_TOO_LARGE,
        msg = "The file size is not allowed",
    )

class AWSS3FailedException :
    ImageException(
        errorCode = 0,
        httpStatusCode = HttpStatus.INTERNAL_SERVER_ERROR,
        msg = "AWS S3 connection failed",
    )

class ImageNotFoundException :
    ImageException(
        errorCode = 0,
        httpStatusCode = HttpStatus.NOT_FOUND,
        msg = "Image metadata not found",
    )

class ImageDeletionForbiddenException :
    ImageException(
        errorCode = 0,
        httpStatusCode = HttpStatus.FORBIDDEN,
        msg = "Only image author can delete the image",
    )

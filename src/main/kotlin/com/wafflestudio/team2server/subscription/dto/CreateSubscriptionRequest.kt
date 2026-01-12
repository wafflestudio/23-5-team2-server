package com.wafflestudio.team2server.subscription.dto

import io.swagger.v3.oas.annotations.media.Schema

data class CreateSubscriptionRequest(
    @Schema(
        description = "구독할 게시판 id",
        required = true,
    )
    val boardId: Long,
)

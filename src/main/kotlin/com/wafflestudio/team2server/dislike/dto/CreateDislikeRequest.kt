package com.wafflestudio.team2server.dislike.dto

import io.swagger.v3.oas.annotations.media.Schema

data class CreateDislikeRequest(
    @Schema(
        description = "싫어요 추가할 게시글 id",
        required = true,
    )
    val articleId: Long,
)

package com.wafflestudio.team2server.like.dto

import io.swagger.v3.oas.annotations.media.Schema

data class CreateLikeRequest(
    @Schema(
        description = "좋아요 추가할 게시글 id",
        required = true,
    )
    val articleId: Long,
)

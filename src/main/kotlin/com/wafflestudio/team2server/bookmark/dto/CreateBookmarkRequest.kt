package com.wafflestudio.team2server.bookmark.dto

import io.swagger.v3.oas.annotations.media.Schema

data class CreateBookmarkRequest(
    @Schema(
        description = "저장할 게시글 id",
        required = true,
    )
    val articleId: Long,
)

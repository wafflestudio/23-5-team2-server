package com.wafflestudio.team2server.article.dto

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "게시글 목록 커서 페이지네이션 요청")
data class ArticlePagingRequest(
    @Schema(
        description = "다음 페이지 커서( 게시글 생성 시간)",
        required = false,
    )
    val nextPublishedAt: Long? = null,
    @Schema(
        description = "다음 페이지 커서(게시글 ID)",
        required = false,
    )
    val nextId: Long? = null,
    @Schema(
        description = "페이지당 게시글 수",
        example = "30",
        required = false,
    )
    val limit: Int = 30,
)

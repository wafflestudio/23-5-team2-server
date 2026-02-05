package com.wafflestudio.team2server.article.dto

import io.swagger.v3.oas.annotations.media.Schema
import java.time.Instant

@Schema(description = "게시글 수정 요청")
data class UpdateArticleRequest(
    @Schema(description = "게시글 제목", example = "수정된 제목")
    val title: String? = null,
    @Schema(description = "게시글 내용", example = "수정된 내용")
    val content: String? = null,
    @Schema(description = "게시글 작성자", example = "다른 작성자")
    val author: String? = null,
    @Schema(description = "원문 게시글 링크", example = "www.youtube.com")
    val originLink: String? = null,
    @Schema(description = "게시글 작성 시각")
    val publishedAt: Instant? = null,
)

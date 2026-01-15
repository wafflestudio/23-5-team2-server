package com.wafflestudio.team2server.article.dto.request

import io.swagger.v3.oas.annotations.media.Schema
import java.time.Instant

@Schema(description = "게시글 생성 요청")
data class CreateArticleRequest(
    @Schema(
        description = "게시글 제목",
        example = "교내 장학금 신청 안내",
        required = true,
    )
    val title: String,
    @Schema(
        description = "게시글 본문 내용",
        example = "mysnu에서 신청하세요...",
        required = true,
    )
    val content: String,
    @Schema(
        description = "기사 작성자",
        example = "행정부",
        required = true,
    )
    val author: String,
    @Schema(
        description = "원문 기사 링크",
        example = "https://www.mysnu/...",
        required = false,
    )
    val originLink: String?,
    @Schema(
        description = "기사 실제 게시 시각",
        example = "2026-01-01T12:00:00Z",
        required = true,
    )
    val publishedAt: Instant,
)

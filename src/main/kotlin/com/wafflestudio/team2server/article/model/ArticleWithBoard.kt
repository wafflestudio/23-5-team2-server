package com.wafflestudio.team2server.article.model
import org.springframework.data.relational.core.mapping.Embedded
import java.time.Instant

data class ArticleWithBoard(
    val id: Long,
    @Embedded.Nullable(prefix = "board_")
    val board: Board?,
    val content: String,
    val author: String,
    val title: String,
    val originLink: String?,
    val publishedAt: Instant,
    val createdAt: Instant,
    val updatedAt: Instant,
) {
    data class Board(
        val id: Long,
        val name: String,
        val sourceUrl: String?,
    )
}

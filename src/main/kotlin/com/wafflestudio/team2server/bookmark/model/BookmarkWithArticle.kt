package com.wafflestudio.team2server.bookmark.model

import com.wafflestudio.team2server.article.model.ArticleWithBoard
import org.springframework.data.relational.core.mapping.Embedded
import java.time.Instant

data class BookmarkWithArticle(
    val id: Long,
    val userId: Long,
    @Embedded.Nullable(prefix = "article_")
    val article: ArticleWithBoard,
    val createdAt: Instant,
)

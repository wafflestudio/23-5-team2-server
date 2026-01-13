package com.wafflestudio.team2server.inboxes.model

import com.wafflestudio.team2server.article.model.ArticleWithBoard
import org.springframework.data.relational.core.mapping.Embedded
import java.time.Instant

data class InboxWithArticle(
    val id: Long,
    val userId: Long,
    @Embedded.Nullable(prefix = "article_")
    val article: ArticleWithBoard,
    val isRead: Boolean,
    val createdAt: Instant,
)

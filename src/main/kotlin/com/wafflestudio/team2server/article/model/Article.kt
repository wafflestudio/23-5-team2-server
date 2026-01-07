package com.wafflestudio.team2server.article.model

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.time.Instant

@Table("articles")
data class Article(
    @Id
    var id: Long? = null,
    var boardId: Long,
    var content: String,
    var author: String,
    var title: String,
    var originLink: String,
    var publishedAt: Instant,
    var createdAt: Instant? = null,
    var updatedAt: Instant? = null,
)

package com.wafflestudio.team2server.article.model

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.time.Instant

@Table("articles")
data class Article(
    @Id
    val id: Long? = null,
    val boardId: Long,
    val content: String,
    val author: String,
    val title: String,
    val originLink: String,
    val publishedAt: Instant,
    val createdAt: Instant? = null,
    val updatedAt: Instant? = null,
)

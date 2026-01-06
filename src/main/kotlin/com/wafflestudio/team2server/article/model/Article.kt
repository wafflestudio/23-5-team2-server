package com.wafflestudio.team2server.article.model

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime

@Table("articles")
data class Article(
    @Id
    val id: Long? = null,
    val boardId: Long,
    val content: String,
    val author: String,
    val title: String,
    val originLink: String,
    val publishedAt: LocalDateTime,
    val createdAt: LocalDateTime? = null,
    val updatedAt: LocalDateTime? = null,
)

package com.wafflestudio.team2server.article.model

import java.time.LocalDateTime

data class Article(
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

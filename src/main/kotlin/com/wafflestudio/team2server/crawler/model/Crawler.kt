package com.wafflestudio.team2server.crawler.model

import java.time.LocalDateTime

data class Crawler(
    val id: Long? = null,
    val boardId: Long,
    val nextUpdateAt: LocalDateTime,
    val createdAt: LocalDateTime? = null,
    val updatedAt: LocalDateTime? = null,
)

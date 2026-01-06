package com.wafflestudio.team2server.crawler.model

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime

@Table("crawlers")
data class Crawler(
    @Id
    val id: Long? = null,
    val boardId: Long,
    val nextUpdateAt: LocalDateTime,
    val createdAt: LocalDateTime? = null,
    val updatedAt: LocalDateTime? = null,
)

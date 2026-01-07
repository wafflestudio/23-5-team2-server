package com.wafflestudio.team2server.crawler.model

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.time.Instant

@Table("crawlers")
data class Crawler(
    @Id
    val id: Long? = null,
    val boardId: Long,
    val nextUpdateAt: Instant,
    val createdAt: Instant? = null,
    val updatedAt: Instant? = null,
)

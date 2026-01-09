package com.wafflestudio.team2server.crawler.dto

import java.time.Instant

data class CrawlerStatusResponse(
    val id: Long? = null,
    val boardName: String? = null,
    val lastUpdatedAt: Instant? = null,
    val nextUpdateAt: Instant? = null,
)

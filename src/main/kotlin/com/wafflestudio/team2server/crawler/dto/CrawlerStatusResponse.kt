package com.wafflestudio.team2server.crawler.dto

import java.time.Instant

data class CrawlerStatusResponse(
    val count: Int,
    val results: List<CrawlerInfo>,
) {
    data class CrawlerInfo(
        val id: Long,
        val boardName: String,
        val lastUpdatedAt: Instant,
        val nextUpdateAt: Instant,
    )
}

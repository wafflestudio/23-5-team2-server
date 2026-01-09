package com.wafflestudio.team2server.crawler.service

import com.wafflestudio.team2server.crawler.dto.CrawlerStatusResponse
import com.wafflestudio.team2server.crawler.repository.CrawlerRepository
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class CrawlerService(
    private val crawlerRepository: CrawlerRepository,
) {
    fun getAllCrawlerStatus(): List<CrawlerStatusResponse> {
        val crawlers = crawlerRepository.findAll()
        return crawlers.map { crawler ->
            CrawlerStatusResponse(
                id = crawler.id ?: 0L,
                boardName = crawler.code,
                lastUpdatedAt = crawler.updatedAt ?: Instant.now(),
                nextUpdateAt = crawler.nextUpdateAt,
            )
        }
    }
}

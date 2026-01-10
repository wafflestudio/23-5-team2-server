package com.wafflestudio.team2server.crawler.service

import com.wafflestudio.team2server.crawler.dto.CrawlerStatusResponse
import com.wafflestudio.team2server.crawler.repository.CrawlerRepository
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class CrawlerService(
    private val crawlerRepository: CrawlerRepository,
) {
    fun getAllCrawlerStatus(): CrawlerStatusResponse {
        val crawlers = crawlerRepository.findAll()

        val crawlerInfos =
            crawlers.map { crawler ->
                CrawlerStatusResponse.CrawlerInfo(
                    id = crawler.id ?: 0L,
                    boardName = crawler.code,
                    lastUpdatedAt = crawler.updatedAt ?: Instant.now(),
                    nextUpdateAt = crawler.nextUpdateAt,
                )
            }

        return CrawlerStatusResponse(
            count = crawlerInfos.size,
            results = crawlerInfos,
        )
    }
}

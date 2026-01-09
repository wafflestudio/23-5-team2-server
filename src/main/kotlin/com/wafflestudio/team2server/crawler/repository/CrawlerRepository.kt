package com.wafflestudio.team2server.crawler.repository

import com.wafflestudio.team2server.article.model.Article
import com.wafflestudio.team2server.crawler.model.Crawler
import org.springframework.data.jdbc.repository.query.Modifying
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.ListCrudRepository
import org.springframework.data.repository.query.Param
import java.time.Instant

interface CrawlerRepository : ListCrudRepository<Crawler, Long> {

    @Modifying
    @Query("UPDATE crawlers SET updated_at = :now, next_update_at = :next WHERE board_id = :boardId")
    fun updateLastCrawledAt(
        @Param("boardId") boardId: Long,
        @Param("now") now: Instant,
        @Param("next") nextUpdateAt: Instant,
    )
}

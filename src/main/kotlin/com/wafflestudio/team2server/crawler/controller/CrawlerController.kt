package com.wafflestudio.team2server.crawler.controller

import com.wafflestudio.team2server.crawler.BaseCrawler
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/crawlers")
class CrawlerController(
    private val crawlers: List<BaseCrawler>,
) {
    @PostMapping("/{crawlerCode}/run")
    fun manualRun(
        @PathVariable crawlerCode: String,
    ): ResponseEntity<String> {
        val targetCrawler =
            crawlers.find { it.code == crawlerCode }
                ?: return ResponseEntity.notFound().build()

        try {
            targetCrawler.crawl()
            return ResponseEntity.ok("크롤러($crawlerCode) 실행 완료!")
        } catch (e: Exception) {
            return ResponseEntity.internalServerError().body(" 실행 실패: ${e.message}")
        }
    }
}

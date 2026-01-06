package com.wafflestudio.team2server.crawler.controller

import com.wafflestudio.team2server.crawler.service.CrawlerService
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/crawler")
class CrawlerController(
    private val crawlerService: CrawlerService,
) {
    /* 테스트용 버튼: 브라우저에서 접속하면 즉시 실행됨
    // 접속 주소: http://localhost:8080/api/crawler/test
    @GetMapping("/test")
    fun runTestCrawler(): String {
        println("수동으로 크롤러를 실행합니다...")

        try {
            crawlerService.crawlMySnu()
            return " 크롤링 성공! (IntelliJ 콘솔 로그를 확요)"
        } catch (e: Exception) {
            return " 에러 발생: ${e.message}"
        }
    }
     */
}

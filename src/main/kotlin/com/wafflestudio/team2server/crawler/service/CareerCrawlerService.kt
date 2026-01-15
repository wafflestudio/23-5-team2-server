package com.wafflestudio.team2server.crawler.service

import com.wafflestudio.team2server.article.model.Article
import com.wafflestudio.team2server.article.repository.ArticleRepository
import com.wafflestudio.team2server.article.service.ArticleService
import com.wafflestudio.team2server.crawler.BaseCrawler
import com.wafflestudio.team2server.crawler.repository.CrawlerRepository
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Service
class CareerCrawlerService(
    crawlerRepository: CrawlerRepository,
    articleRepository: ArticleRepository,
    articleService: ArticleService,
) : BaseCrawler(crawlerRepository, articleRepository, articleService) {
    override val listUrl = "https://career.snu.ac.kr/center/community/notice"
    override val baseUrl = "https://career.snu.ac.kr"
    override val targetBoardId = 4L
    override val code = "career"
    override val crawlIntervalSeconds = 86400L

    val detailDateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    override fun getPostList(document: Document): List<Element> = document.select("table.t_list tbody tr")

    override fun getPostTitle(element: Element): String {
        val pTag = element.select("td.t_left.tit_td a.tit p")
        return if (pTag.hasText()) {
            pTag.text().trim()
        } else {
            element.select("td.t_left.tit_td a.tit").text().trim()
        }
    }

    override fun getPostLink(element: Element): String {
        val rawUrl = element.select("td.t_left.tit_td a.tit").attr("href")
        return if (rawUrl.startsWith("http")) rawUrl else "$baseUrl$rawUrl"
    }

    override fun parseDetailAndGetArticle(
        boardId: Long,
        element: Element,
        detailDoc: Document,
        url: String,
        title: String,
    ): Article {
        val content = detailDoc.select("table.t_view td.bt_none > div").html()

        var author = detailDoc.select("table.t_view th:contains(작성자) + td").text().trim()
        if (author.isEmpty()) {
            author = "관리자" // fallback
        }

        val dateStr = detailDoc.select("table.t_view th:contains(작성일) + td").text().trim()

        val publishedAt =
            try {
                val localDate = LocalDate.parse(dateStr, detailDateFormatter)
                localDate.atStartOfDay(ZoneId.of("Asia/Seoul")).toInstant()
            } catch (e: Exception) {
                Instant.now()
            }

        return Article(
            boardId = boardId,
            title = title,
            content = content,
            author = author,
            originLink = url,
            publishedAt = publishedAt,
            createdAt = Instant.now(),
            updatedAt = Instant.now(),
        )
    }

    @Scheduled(fixedRate = 86400000)
    fun runScheduled() {
        crawl()
        updateExecutionTime()
    }
}

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
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Service
class MysnuCrawlerService(
    crawlerRepository: CrawlerRepository,
    articleRepository: ArticleRepository,
    articleService: ArticleService,
) : BaseCrawler(crawlerRepository, articleRepository, articleService) {
    override val listUrl = "https://my.snu.ac.kr/ctt/bb/bulletin?b=1&ls=20&ln=1&dm=m&inB=&inPx="
    override val baseUrl = "https://my.snu.ac.kr"
    override val targetBoardId = 2L
    override val code = "mysnu"
    override val crawlIntervalSeconds = 3600L

    val formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm:ss")

    override fun getPostList(document: Document): List<Element> {
        val list = document.select("ul[data-name='post_list']")
        return list
    }

    override fun getPostTitle(element: Element): String = element.select("li.bc-s-title .postT span").text().trim()

    override fun getPostLink(element: Element): String {
        val rawUrl = element.attr("data-url")
        return if (rawUrl.startsWith("http")) rawUrl else "$baseUrl$rawUrl"
    }

    override fun parseDetailAndGetArticle(
        boardId: Long,
        element: Element,
        detailDoc: Document,
        url: String,
        title: String,
    ): Article {
        val content = detailDoc.select("div.text_area").html()

        var author = element.select("li.bc-s-cre_user_name").text().trim()
        var dateStr = ""

        val metaList = detailDoc.select(".sub_search_box.type03 dl")
        for (dl in metaList) {
            val label = dl.select("dt").text()
            val value = dl.select("dd").text()
            if (label.contains("Create User")) author = value
            if (label.contains("Created Date")) dateStr = value
        }

        val publishedAt =
            try {
                val localDateTime = LocalDateTime.parse(dateStr, formatter)
                localDateTime.atZone(ZoneId.of("Asia/Seoul")).toInstant()
            } catch (_: Exception) {
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

    @Scheduled(fixedRate = 3600000)
    override fun runScheduled() {
        super.runScheduled()
    }
}

package com.wafflestudio.team2server.crawler.service

import com.wafflestudio.team2server.article.model.Article
import com.wafflestudio.team2server.article.repository.ArticleRepository
import com.wafflestudio.team2server.article.service.ArticleService
import com.wafflestudio.team2server.crawler.BaseCrawler
import com.wafflestudio.team2server.crawler.repository.CrawlerRepository
import org.jsoup.Connection
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.regex.Pattern

@Service
class SnutiCrawlerService(
    crawlerRepository: CrawlerRepository,
    private val articleRepository: ArticleRepository,
    private val articleService: ArticleService,
) : BaseCrawler(crawlerRepository, articleRepository, articleService) {
    override val listUrl = "https://snuti.snu.ac.kr"
    override val baseUrl = "https://snuti.snu.ac.kr"
    override val targetBoardId = 5L
    override val code = "snuti"
    override val crawlIntervalSeconds = 3600L

    val detailDateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    override fun getPostList(document: Document): List<Element> = document.select("#notice1 > div > div > article")

    override fun getPostTitle(element: Element): String {
        val aTag = element.select("a")
        return aTag.text().trim()
    }

    override fun getPostLink(element: Element): String {
        val rawUrl = element.select("a").attr("href")
        return if (rawUrl.startsWith("http")) rawUrl else "$baseUrl$rawUrl"
    }

    override fun parseDetailAndGetArticle(
        boardId: Long,
        element: Element,
        detailDoc: Document,
        url: String,
        title: String,
    ): Article {
        val content = detailDoc.select("div.board_view_content").html()
        println(detailDoc)

        var author = detailDoc.select("span.writer").text().trim()
        if (author.isEmpty()) {
            author = "관리자" // fallback
        }

        val dateStr = detailDoc.select("span.date").text().trim()

        val publishedAt =
            try {
                val localDate = LocalDate.parse(dateStr, detailDateFormatter)
                localDate.atStartOfDay(ZoneId.of("Asia/Seoul")).toInstant()
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

    fun fetchDetail(url: String): Document {
        // 1. Fetch the initial page
        val firstPage =
            Jsoup
                .connect(url)
                .userAgent("Mozilla/5.0")
                .get()

        println(url)
        println(firstPage)

        // 2. Locate the script containing the redirection call
        // We search for the script tag that calls go_board_view
        val scripts = firstPage.select("script")
        var bid = ""
        var secondPageUrl = ""

        // Regex to find: go_board_view('VALUE1', 'VALUE2')
        val pattern = Pattern.compile("go_board_view\\s*\\(\\s*'([^']*)'\\s*,\\s*'([^']*)'")

        for (script in scripts) {
            val scriptContent: String = script.data()
            if (scriptContent.contains("go_board_view")) {
                val matcher = pattern.matcher(scriptContent)
                if (matcher.find()) {
                    bid = matcher.group(1) // '12698'
                    secondPageUrl = matcher.group(2) // 'https://snuti.snu.ac.kr/...'
                    break
                }
            }
        }

        if (bid.isEmpty() || secondPageUrl.isEmpty()) {
            throw IllegalArgumentException("bid or second page url can not be empty")
        }

        // 3. If we found the values, perform the POST request
        val realPage =
            Jsoup
                .connect(secondPageUrl)
                .data("board_mode", "VIEW")
                .data("bid", bid)
                .data("var_page", "1")
                .data("search_field", "ALL")
                .data("search_task", "ALL")
                .method(Connection.Method.POST)
                .post()
        println(realPage)
        return realPage
    }

    override fun crawl() {
        try {
            val listDoc = fetch(listUrl)

            val rows = getPostList(listDoc)

            for (row in rows) {
                val rawLink = getPostLink(row)
                val detailUrl = if (rawLink.startsWith("http")) rawLink else "$baseUrl$rawLink"

                if (articleRepository.existsByOriginLink(detailUrl)) {
                    continue
                }

                val detailDoc = fetchDetail(detailUrl)

                val title = getPostTitle(row)

                val article = parseDetailAndGetArticle(targetBoardId, row, detailDoc, detailUrl, title)

                articleService.saveNewArticle(article)

                Thread.sleep(500)
            }
        } catch (_: Exception) {
        }
    }

    @Scheduled(fixedRate = 3600000)
    fun runScheduled() {
        crawl()
        updateExecutionTime()
    }
}

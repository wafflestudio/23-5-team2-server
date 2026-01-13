package com.wafflestudio.team2server.crawler.service

import com.wafflestudio.team2server.article.model.Article
import com.wafflestudio.team2server.article.repository.ArticleRepository
import com.wafflestudio.team2server.article.service.ArticleService
import com.wafflestudio.team2server.crawler.BaseCrawler
import com.wafflestudio.team2server.crawler.repository.CrawlerRepository
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Service
class CseCrawlerService(
    crawlerRepository: CrawlerRepository,
    private val articleRepository: ArticleRepository,
    private val articleService: ArticleService,
) : BaseCrawler(crawlerRepository, articleRepository, articleService) {
    private val log = LoggerFactory.getLogger(this::class.java)

    override val listUrl = "https://cse.snu.ac.kr/community/notice"
    override val baseUrl = "https://cse.snu.ac.kr"
    override val targetBoardId = 3L
    override val code = "cse"
    override val crawlIntervalSeconds = 3600L

    private val detailDateFormatter =
        DateTimeFormatter
            .ofPattern("yyyy/M/d a h:mm")
            .withLocale(java.util.Locale.KOREAN)

    override fun crawl() {
        var page = 1
        var collectedCount = 0
        val targetCount = 20

        try {
            while (collectedCount < targetCount) {
                val pageUrl = "$listUrl?pageNum=$page"

                val document = fetch(pageUrl)

                val rows = getPostList(document)
                if (rows.isEmpty()) break

                for (row in rows) {
                    if (collectedCount >= targetCount) break

                    val tempTitle = getPostTitle(row)

                    if (tempTitle.isBlank()) {
                        continue
                    }

                    if (isPinnedPost(row)) {
                        continue
                    }

                    val rawLink = getPostLink(row)
                    val detailUrl = if (rawLink.startsWith("http")) rawLink else "$baseUrl$rawLink"

                    if (!articleRepository.existsByOriginLink(detailUrl)) {
                        val title = getPostTitle(row)

                        val detailDoc = fetch(detailUrl)

                        val article = parseDetailAndGetArticle(targetBoardId, row, detailDoc, detailUrl, title)
                        articleService.saveNewArticle(article)
                    }

                    collectedCount++
                }

                page++
                Thread.sleep(500)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            updateExecutionTime()
        }
    }

    private fun isPinnedPost(element: Element): Boolean = element.selectFirst("span:first-child svg") != null

    override fun getPostList(document: Document): List<Element> =
        document.select("ul li:has(a[href*='/community/notice']):has(span:matches(\\d{4}/\\d{1,2}/\\d{1,2}))")

    override fun getPostTitle(element: Element): String {
        val anchor = element.selectFirst("a") ?: return ""

        val title = anchor.text().trim()

        return title
    }

    override fun getPostLink(element: Element): String = element.selectFirst("a")?.attr("href") ?: ""

    override fun parseDetailAndGetArticle(
        boardId: Long,
        element: Element,
        detailDoc: Document,
        url: String,
        title: String,
    ): Article {
        val content =
            detailDoc.selectFirst(".sun-editor-editable")?.html()
                ?: detailDoc.selectFirst("div.field-name-body")?.html()
                ?: ""

        val metaInfoParagraphs = detailDoc.select("div.flex.gap-5.text-sm p")

        val author =
            metaInfoParagraphs
                .firstOrNull { it.text().contains("작성자") }
                ?.text()
                ?.replace("작성자", "")
                ?.replace(":", "")
                ?.trim()
                ?: "CSE"

        val dateTextRaw =
            metaInfoParagraphs.firstOrNull { it.text().contains("작성 날짜") }?.text()
                ?: ""

        val publishedAt =
            try {
                if (dateTextRaw.isNotBlank()) {
                    val cleanDateStr =
                        dateTextRaw
                            .replace("작성 날짜", "")
                            .replace(Regex("\\(.*\\)"), "")
                            .trim()
                            .removePrefix(":")
                            .trim()
                            .replace(Regex("\\s+"), " ")
                    val localDateTime = LocalDateTime.parse(cleanDateStr, detailDateFormatter)
                    localDateTime.atZone(ZoneId.of("Asia/Seoul")).toInstant()
                } else {
                    java.time.Instant.now()
                }
            } catch (_: Exception) {
                java.time.Instant.now()
            }

        return Article(
            boardId = boardId,
            title = title,
            content = content,
            author = author,
            originLink = url,
            publishedAt = publishedAt,
            createdAt = java.time.Instant.now(),
            updatedAt = java.time.Instant.now(),
        )
    }

    @Scheduled(fixedRate = 3600000)
    fun runScheduled() {
        crawl()
    }
}

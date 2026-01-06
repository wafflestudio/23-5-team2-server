package com.wafflestudio.team2server.crawler

import com.wafflestudio.team2server.article.model.Article
import com.wafflestudio.team2server.article.repository.ArticleRepository
import com.wafflestudio.team2server.crawler.repository.CrawlerRepository
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import java.time.LocalDateTime

abstract class BaseCrawler(
    private val crawlerRepository: CrawlerRepository,
    private val articleRepository: ArticleRepository,
) {
    abstract val listUrl: String
    abstract val baseUrl: String
    abstract val targetBoardId: Long
    abstract val code: String

    protected abstract fun getPostList(document: Document): List<Element>

    protected abstract fun getPostTitle(element: Element): String

    protected abstract fun getPostLink(element: Element): String

    protected abstract fun parseDetailAndGetArticle(
        boardId: Long,
        element: Element,
        detailDoc: Document,
        url: String,
        title: String,
    ): Article

    fun crawl() {
        try {
            val listDoc = connect(listUrl)

            val rows = getPostList(listDoc)

            for (row in rows) {
                val rawLink = getPostLink(row)
                val detailUrl = if (rawLink.startsWith("http")) rawLink else "$baseUrl$rawLink"

                if (crawlerRepository.existsByOriginLink(detailUrl)) {
                    continue
                }

                val detailDoc = connect(detailUrl)

                val title = getPostTitle(row)

                val article = parseDetailAndGetArticle(targetBoardId, row, detailDoc, detailUrl, title)

                articleRepository.save(article)

                Thread.sleep(500)
            }
        } catch (e: Exception) {
        }
    }

    private fun connect(url: String): Document =
        Jsoup
            .connect(url)
            .userAgent("Mozilla/5.0 ...")
            .timeout(10000)
            .get()

    protected fun updateExecutionTime() {
        try {
            crawlerRepository.updateLastCrawledAt(targetBoardId, LocalDateTime.now())
        } catch (e: Exception) {
        }
    }
}

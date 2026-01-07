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
    abstract val crawlIntervalSeconds: Long

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

    /**
     * 해당 사이트의 게시글 목록을 가져와 크롤링을 수행합니다.
     *
     * 기본 구현은 목록을 한 번만 조회(fetch)하도록 되어 있습니다.
     * 대부분의 사이트는 최신 공지가 첫 페이지에 위치하므로, 별도의 수정 없이 이 메서드를 사용하면 됩니다.
     *
     * 단, '컴퓨터공학부' 사이트와 같이 공지사항 확인을 위해 여러 페이지를 넘겨야(pagination) 하는 경우,
     * 이 메서드를 override 하여 사이트 특성에 맞는 탐색 로직을 직접 구현해야 합니다.
     */
    open fun crawl() {
        try {
            val listDoc = fetch(listUrl)

            val rows = getPostList(listDoc)

            for (row in rows) {
                val rawLink = getPostLink(row)
                val detailUrl = if (rawLink.startsWith("http")) rawLink else "$baseUrl$rawLink"

                if (crawlerRepository.existsByOriginLink(detailUrl)) {
                    continue
                }

                val detailDoc = fetch(detailUrl)

                val title = getPostTitle(row)

                val article = parseDetailAndGetArticle(targetBoardId, row, detailDoc, detailUrl, title)

                articleRepository.save(article)

                Thread.sleep(500)
            }
        } catch (e: Exception) {
        }
    }

    private fun fetch(url: String): Document =
        Jsoup
            .connect(url)
            .userAgent("Mozilla/5.0 ...")
            .timeout(10000)
            .get()

    protected fun updateExecutionTime() {
        try {
            val now = LocalDateTime.now()
            val next = now.plusSeconds(crawlIntervalSeconds)
            crawlerRepository.updateLastCrawledAt(targetBoardId, now, next)
        } catch (e: Exception) {
        }
    }
}

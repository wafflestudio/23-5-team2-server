package com.wafflestudio.team2server.crawler.service

import com.wafflestudio.team2server.article.model.Article
import com.wafflestudio.team2server.crawler.repository.CrawlerRepository
import org.jsoup.Jsoup
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Service
class CrawlerService(
    private val repository: CrawlerRepository,
) {
    private val detailFormatter = DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm:ss")
    private val listFormatter = DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm:ss")

    @Scheduled(fixedRate = 3600000)
    fun crawlMySnu() {
        println(" mySNU 전체공지 게시판 크롤링 시작...")

        val listUrl = "https://my.snu.ac.kr/ctt/bb/bulletin?b=1&ls=20&ln=1&dm=m&inB=&inPx="
        val baseUrl = "https://my.snu.ac.kr"
        val targetBoardId = 1L

        try {
            val listDoc =
                Jsoup
                    .connect(listUrl)
                    .userAgent(
                        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/110.0.0.0 Safari/537.36",
                    ).timeout(10000)
                    .get()

            val noticeRows = listDoc.select("ul[data-name='post_list']")

            for (row in noticeRows) {
                val rawUrl = row.attr("data-url")
                if (rawUrl.isBlank()) continue

                val detailUrl = if (rawUrl.startsWith("http")) rawUrl else "$baseUrl$rawUrl"

                if (repository.existsByLink(detailUrl)) {
                    println("여기서부터는 이미 저장된 글 -> 크롤링을 종료")
                    return
                }

                val title = row.select("li.bc-s-title .postT span").text().trim()
                val author = row.select("li.bc-s-cre_user_name").text().trim()
                val dateStr = row.select("li.bc-s-cre_dt").text().trim()

                println(" 새 글 발견: $title")

                crawlDetailAndSave(targetBoardId, detailUrl, title, author, dateStr)

                Thread.sleep(500)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            println(" 크롤링 에러: ${e.message}")
        }
    }

    private fun crawlDetailAndSave(
        boardId: Long,
        url: String,
        listTitle: String,
        listAuthor: String,
        listDateStr: String,
    ) {
        try {
            val doc =
                Jsoup
                    .connect(url)
                    .userAgent(
                        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/110.0.0.0 Safari/537.36",
                    ).timeout(5000)
                    .get()

            val content = doc.select("div.text_area").html()

            var finalAuthor = listAuthor
            var detailedDateStr = ""

            val metaList = doc.select(".sub_search_box.type03 dl")
            for (dl in metaList) {
                val label = dl.select("dt").text().trim()
                val value = dl.select("dd").text().trim()

                if (label.contains("Create User", ignoreCase = true)) {
                    finalAuthor = value
                } else if (label.contains("Created Date", ignoreCase = true)) {
                    detailedDateStr = value
                }
            }

            val publishedAt: LocalDateTime =
                try {
                    if (detailedDateStr.isNotBlank()) {
                        LocalDateTime.parse(detailedDateStr, detailFormatter)
                    } else {
                        val datePart = listDateStr.replace("-", ".")
                        LocalDateTime.parse("$datePart 00:00:00", listFormatter)
                    }
                } catch (e: Exception) {
                    println(" 날짜 파싱 실패 ($detailedDateStr), 현재 시간으로 대체.")
                    LocalDateTime.now()
                }

            if (content.isNotBlank()) {
                val article =
                    Article(
                        boardId = boardId,
                        title = listTitle,
                        content = content,
                        author = finalAuthor,
                        originLink = url,
                        publishedAt = publishedAt,
                    )

                val newId = repository.saveArticle(article)

                println("  저장 완료!(ID: $newId)")
            }
        } catch (e: Exception) {
            println("  상세 수집 실패: ${e.message}")
        }
    }
}

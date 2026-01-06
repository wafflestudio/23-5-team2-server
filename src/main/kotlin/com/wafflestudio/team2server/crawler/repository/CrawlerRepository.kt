package com.wafflestudio.team2server.crawler.repository

import com.wafflestudio.team2server.article.model.Article
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.jdbc.support.GeneratedKeyHolder
import org.springframework.stereotype.Repository

@Repository
class CrawlerRepository(
    private val jdbc: NamedParameterJdbcTemplate,
) {
    fun existsByLink(link: String): Boolean {
        val sql = "SELECT count(*) FROM articles WHERE origin_link = :link"
        val count = jdbc.queryForObject(sql, mapOf("link" to link), Int::class.java) ?: 0
        return count > 0
    }

    fun saveArticle(article: Article): Long {
        val sql = """
            INSERT INTO articles (board_id, title, content, author, origin_link, published_at)
            VALUES (:boardId, :title, :content, :author, :originLink, :publishedAt)
        """

        val params =
            MapSqlParameterSource()
                .addValue("boardId", article.boardId)
                .addValue("title", article.title)
                .addValue("content", article.content)
                .addValue("author", article.author)
                .addValue("originLink", article.originLink)
                .addValue("publishedAt", article.publishedAt)

        val keyHolder = GeneratedKeyHolder()

        jdbc.update(sql, params, keyHolder)

        return keyHolder.key?.toLong() ?: 0L
    }
}

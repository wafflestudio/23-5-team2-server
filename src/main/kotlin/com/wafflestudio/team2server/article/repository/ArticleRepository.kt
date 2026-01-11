package com.wafflestudio.team2server.article.repository

import com.wafflestudio.team2server.article.model.Article
import com.wafflestudio.team2server.article.model.ArticleWithBoard
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.ListCrudRepository
import org.springframework.data.repository.query.Param
import java.time.Instant

interface ArticleRepository : ListCrudRepository<Article, Long> {
    @Query(
        """
    SELECT
        a.id                AS id,
        a.content           AS content,
        a.author            AS author,
        a.title             AS title,
        a.origin_link       AS origin_link,
        a.published_at      AS published_at,
        a.created_at        AS created_at,
        a.updated_at        AS updated_at,

        b.id                AS board_id,
        b.name              AS board_name,
        b.source_url        AS board_source_url
    FROM articles a
    LEFT JOIN boards b
        ON a.board_id = b.id
    WHERE a.id = :articleId
    """,
    )
    fun findByIdWithBoard(
        @Param("articleId") articleId: Long,
    ): ArticleWithBoard?

    @Query(
        """
    SELECT
        a.id            AS id,
        a.title         AS title,
        a.content       AS content,
        a.author        AS author,
        a.origin_link   AS origin_link,
        a.published_at  AS published_at,
        a.created_at    AS created_at,
        a.updated_at    AS updated_at,

        b.id            AS board_id,
        b.name          AS board_name,
        b.source_url    AS board_source_url
    FROM articles a
    LEFT JOIN boards b
        ON a.board_id = b.id
    WHERE a.board_id = :boardId
      AND (:nextPublishedAt IS NULL OR (a.published_at, a.id) < (:nextPublishedAt, :nextId))
    ORDER BY a.published_at DESC, a.id DESC
    LIMIT :limit
    """,
    )
    fun findByBoardIdWithCursor(
        @Param("boardId") boardId: Long,
        @Param("nextPublishedAt") nextPublishedAt: Instant?,
        @Param("nextId") nextId: Long?,
        @Param("limit") limit: Int,
    ): List<ArticleWithBoard>

    fun existsByOriginLink(originLink: String): Boolean
}

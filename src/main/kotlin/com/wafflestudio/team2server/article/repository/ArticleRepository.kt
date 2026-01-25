package com.wafflestudio.team2server.article.repository

import com.wafflestudio.team2server.article.model.Article
import com.wafflestudio.team2server.article.model.ArticleWithBoard
import org.springframework.data.jdbc.repository.query.Modifying
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
        a.views             AS views,
        a.origin_link       AS origin_link,
        a.published_at      AS published_at,
        a.created_at        AS created_at,
        a.updated_at        AS updated_at,

        b.id                AS board_id,
        b.name              AS board_name,
        b.source_url        AS board_source_url,
        
        (SELECT COUNT(*) FROM dislikes d WHERE d.article_id = a.id) AS dislikes,
        (SELECT COUNT(*) FROM likes d WHERE d.article_id = a.id) AS likes
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
    a.views         AS views,
    a.origin_link   AS origin_link,
    a.published_at  AS published_at,
    a.created_at    AS created_at,
    a.updated_at    AS updated_at,

    b.id            AS board_id,
    b.name          AS board_name,
    b.source_url    AS board_source_url,
    
    (SELECT COUNT(*) FROM dislikes d WHERE d.article_id = a.id) AS dislikes,
    (SELECT COUNT(*) FROM likes d WHERE d.article_id = a.id) AS likes
FROM articles a
LEFT JOIN boards b
    ON a.board_id = b.id
WHERE
  ( :boardFilter = FALSE OR a.board_id IN (:boardIds) )
  AND (
      :keyword IS NULL
      OR a.title LIKE CONCAT('%', :keyword, '%')
      OR a.content LIKE CONCAT('%', :keyword, '%')
  )
  AND (:nextPublishedAt IS NULL OR (a.published_at, a.id) < (:nextPublishedAt, :nextId))
ORDER BY a.published_at DESC, a.id DESC
LIMIT :limit
""",
    )
    fun findByBoardIdsWithCursor(
        @Param("boardFilter") boardFilter: Boolean,
        @Param("boardIds") boardIds: List<Long>,
        @Param("keyword") keyword: String?,
        @Param("nextPublishedAt") nextPublishedAt: Instant?,
        @Param("nextId") nextId: Long?,
        @Param("limit") limit: Int,
    ): List<ArticleWithBoard>

    fun existsByOriginLink(originLink: String): Boolean

    fun deleteAllByBoardId(boardId: Long)

    @Query("SELECT * FROM articles WHERE id = :id FOR UPDATE")
    fun findByIdForUpdate(
        @Param("id") id: Long,
    ): Article?

    // Do not use save directly. Instead, use ArticleService.saveNewArticle. It triggers ArticleCreatedEvent and the inbox adding logic.

    @Modifying
    @Query("UPDATE articles SET views = views + 1 WHERE id = :articleId")
    fun increaseViews(
        @Param("articleId") articleId: Long,
    ): Int

    @Query(
        """
SELECT
    a.id            AS id,
    a.title         AS title,
    a.content       AS content,
    a.author        AS author,
    a.views         AS views,
    a.origin_link   AS origin_link,
    a.published_at  AS published_at,
    a.created_at    AS created_at,
    a.updated_at    AS updated_at,

    b.id            AS board_id,
    b.name          AS board_name,
    b.source_url    AS board_source_url,
    
    (SELECT COUNT(*) FROM dislikes d WHERE d.article_id = a.id) AS dislikes,
    (SELECT COUNT(*) FROM likes l WHERE l.article_id = a.id) AS likes
FROM articles a
LEFT JOIN boards b
    ON a.board_id = b.id
WHERE
  (
      :keyword IS NULL
      OR a.title LIKE CONCAT('%', :keyword, '%')
      OR a.content LIKE CONCAT('%', :keyword, '%')
  )
  AND a.views >= (:hotScore / :viewsWeight)

  AND (
      :nextPublishedAt IS NULL
      OR (a.published_at, a.id) < (:nextPublishedAt, :nextId)
  )

ORDER BY a.published_at DESC, a.id DESC
LIMIT :limit
""",
    )
    fun findHotsWithCursor(
        @Param("keyword") keyword: String?,
        @Param("nextPublishedAt") nextPublishedAt: Instant?,
        @Param("nextId") nextId: Long?,
        @Param("limit") limit: Int,
        @Param("hotScore") hotScore: Long,
        @Param("viewsWeight") viewsWeight: Double,
    ): List<ArticleWithBoard>
}

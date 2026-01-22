package com.wafflestudio.team2server.bookmark.repository

import com.wafflestudio.team2server.bookmark.model.Bookmark
import com.wafflestudio.team2server.bookmark.model.BookmarkWithArticle
import org.springframework.data.jdbc.repository.query.Modifying
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.ListCrudRepository
import org.springframework.data.repository.query.Param

interface BookmarkRepository : ListCrudRepository<Bookmark, Long> {
    @Query(
        """
    SELECT
        bm.id AS id,
        bm.user_id AS user_id,
        bm.created_at AS created_at,
        
        a.id            AS article_id,
        a.title         AS article_title,
        a.content       AS article_content,
        a.author        AS article_author,
        a.origin_link   AS article_origin_link,
        a.published_at  AS article_published_at,
        a.created_at    AS article_created_at,
        a.updated_at    AS article_updated_at,

        b.id            AS article_board_id,
        b.name          AS article_board_name,
        b.source_url    AS article_board_source_url,
    
    (SELECT COUNT(*) FROM dislikes d WHERE d.article_id = a.id) AS article_dislikes,
    (SELECT COUNT(*) FROM likes d WHERE d.article_id = a.id) AS article_likes
    FROM bookmarks bm  
    LEFT JOIN articles a
        ON bm.article_id = a.id
    LEFT JOIN boards b
        ON a.board_id = b.id
    WHERE bm.user_id = :userId
      AND (:nextId IS NULL OR bm.id < :nextId)
    ORDER BY bm.id DESC
    LIMIT :limit
    """,
    )
    fun findByUserIdWithCursor(
        @Param("userId") userId: Long,
        @Param("nextId") nextId: Long?,
        @Param("limit") limit: Int,
    ): List<BookmarkWithArticle>

    fun existsByIdAndUserId(
        id: Long,
        userId: Long,
    ): Boolean

    /**
     * Inserts a record or ignores it if the primary key or a
     * unique constraint already exists.
     */
    @Modifying
    @Query("INSERT IGNORE INTO bookmarks (user_id, article_id) VALUES (:user_id, :article_id)")
    fun insertOrIgnore(
        @Param("user_id") userId: Long,
        @Param("article_id") articleId: Long,
    ): Int
}

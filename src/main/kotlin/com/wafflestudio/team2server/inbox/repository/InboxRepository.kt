package com.wafflestudio.team2server.inbox.repository

import com.wafflestudio.team2server.inbox.model.Inbox
import com.wafflestudio.team2server.inbox.model.InboxWithArticle
import org.springframework.data.jdbc.repository.query.Modifying
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.ListCrudRepository
import org.springframework.data.repository.query.Param
import org.springframework.transaction.annotation.Transactional

interface InboxRepository : ListCrudRepository<Inbox, Long> {
    @Query(
        """
    SELECT
        i.id AS id,
        i.user_id AS user_id,
        i.is_read AS is_read,
        i.created_at AS created_at,
        
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
        b.source_url    AS article_board_source_url
    FROM inboxes i  
    LEFT JOIN articles a
        ON i.article_id = a.id
    LEFT JOIN boards b
        ON a.board_id = b.id
    WHERE i.user_id = :userId
      AND (:nextId IS NULL OR i.id < :nextId)
    ORDER BY i.id DESC
    LIMIT :limit
    """,
    )
    fun findByUserIdWithCursor(
        @Param("userId") userId: Long,
        @Param("nextId") nextId: Long?,
        @Param("limit") limit: Int,
    ): List<InboxWithArticle>

    fun findByIdAndUserId(
        id: Long,
        userId: Long,
    ): Inbox?

    @Modifying
    @Transactional
    @Query(
        value = """
        INSERT INTO inboxes (user_id, article_id)
        SELECT s.user_id, :articleId
        FROM subscriptions s
        WHERE s.board_id = :boardId
    """,
    )
    fun createInboxesForBoardSubscribers(
        @Param("articleId") articleId: Long,
        @Param("boardId") boardId: Long,
    ): Int
}

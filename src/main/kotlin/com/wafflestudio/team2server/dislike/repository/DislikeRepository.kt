package com.wafflestudio.team2server.dislike.repository

import com.wafflestudio.team2server.dislike.model.Dislike
import org.springframework.data.jdbc.repository.query.Modifying
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.ListCrudRepository
import org.springframework.data.repository.query.Param

interface DislikeRepository : ListCrudRepository<Dislike, Long> {
    /**
     * Inserts a record or ignores it if the primary key or a
     * unique constraint already exists.
     */
    @Modifying
    @Query("INSERT IGNORE INTO dislikes (user_id, article_id) VALUES (:user_id, :article_id)")
    fun insertOrIgnore(
        @Param("user_id") userId: Long,
        @Param("article_id") articleId: Long,
    ): Int

    /**
     * Deletes a record or ignores it if the primary key or a
     * unique constraint already exists.
     */
    @Modifying
    @Query("DELETE IGNORE FROM dislikes WHERE article_id = :article_id AND user_id = :user_id")
    fun deleteOrIgnore(
        @Param("user_id") userId: Long,
        @Param("article_id") articleId: Long,
    ): Int

    fun existsByArticleIdAndUserId(
        articleId: Long,
        userId: Long,
    ): Boolean

    fun countByArticleId(articleId: Long): Int
}

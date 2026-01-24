package com.wafflestudio.team2server.like.repository

import com.wafflestudio.team2server.like.model.Like
import org.springframework.data.jdbc.repository.query.Modifying
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.ListCrudRepository
import org.springframework.data.repository.query.Param

interface LikeRepository : ListCrudRepository<Like, Long> {
    @Modifying
    @Query("INSERT IGNORE INTO likes (user_id, article_id) VALUES (:user_id, :article_id)")
    fun insertOrIgnore(
        @Param("user_id") userId: Long,
        @Param("article_id") articleId: Long,
    ): Int

    @Modifying
    @Query("DELETE IGNORE FROM likes WHERE article_id = :article_id AND user_id = :user_id")
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

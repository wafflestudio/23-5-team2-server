package com.wafflestudio.team2server.dislike.service

import com.wafflestudio.team2server.article.repository.ArticleRepository
import com.wafflestudio.team2server.dislike.repository.DislikeRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class DislikeService(
    private val dislikeRepository: DislikeRepository,
    private val articleRepository: ArticleRepository,
) {
    @Transactional
    fun deleteDislike(
        userId: Long,
        articleId: Long,
    ): Int {
        // 1. Lock the Article. Every thread for this articleId will queue here
        articleRepository.findByIdForUpdate(articleId)

        // 2. Now delete is safe because only one thread is active for this articleId
        dislikeRepository.deleteOrIgnore(userId, articleId)

        // 3. Return the current count
        return dislikeRepository.countByArticleId(articleId)
    }

    fun createDislike(
        userId: Long,
        articleId: Long,
    ): Int {
        dislikeRepository.insertOrIgnore(userId, articleId)
        return dislikeRepository.countByArticleId(articleId)
    }

    fun existsByArticleIdAndUserId(
        articleId: Long,
        userId: Long,
    ): Boolean = dislikeRepository.existsByArticleIdAndUserId(articleId, userId)
}

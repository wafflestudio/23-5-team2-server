package com.wafflestudio.team2server.like.service

import com.wafflestudio.team2server.article.repository.ArticleRepository
import com.wafflestudio.team2server.like.repository.LikeRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class LikeService(
    private val likeRepository: LikeRepository,
    private val articleRepository: ArticleRepository,
) {
    @Transactional
    fun deleteLike(
        userId: Long,
        articleId: Long,
    ): Int {
        articleRepository.findByIdForUpdate(articleId)

        likeRepository.deleteOrIgnore(userId, articleId)

        return likeRepository.countByArticleId(articleId)
    }

    fun createLike(
        userId: Long,
        articleId: Long,
    ): Int {
        likeRepository.insertOrIgnore(userId, articleId)
        return likeRepository.countByArticleId(articleId)
    }

    fun existsByArticleIdAndUserId(
        articleId: Long,
        userId: Long,
    ): Boolean = likeRepository.existsByArticleIdAndUserId(articleId, userId)
}

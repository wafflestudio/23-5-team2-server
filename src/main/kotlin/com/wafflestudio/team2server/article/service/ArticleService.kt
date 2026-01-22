package com.wafflestudio.team2server.article.service

import com.wafflestudio.team2server.article.ArticleBlankAuthorException
import com.wafflestudio.team2server.article.ArticleBlankContentException
import com.wafflestudio.team2server.article.ArticleBlankOriginLinkException
import com.wafflestudio.team2server.article.ArticleBlankPublishedException
import com.wafflestudio.team2server.article.ArticleBlankTitleException
import com.wafflestudio.team2server.article.ArticleNotFoundException
import com.wafflestudio.team2server.article.dto.ArticlePaging
import com.wafflestudio.team2server.article.dto.core.ArticleDto
import com.wafflestudio.team2server.article.dto.response.ArticlePagingResponse
import com.wafflestudio.team2server.article.model.Article
import com.wafflestudio.team2server.article.model.ArticleCreatedEvent
import com.wafflestudio.team2server.article.repository.ArticleRepository
import com.wafflestudio.team2server.board.BoardNotFoundException
import com.wafflestudio.team2server.board.repository.BoardRepository
import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Service
class ArticleService(
    private val articleRepository: ArticleRepository,
    private val boardRepository: BoardRepository,
    private val eventPublisher: ApplicationEventPublisher,
) {
    @Transactional
    fun get(articleId: Long): ArticleDto {
        val updated = articleRepository.increaseViews(articleId)
        if (updated == 0) throw ArticleNotFoundException()

        val articleWithBoard =
            articleRepository.findByIdWithBoard(articleId)
                ?: throw ArticleNotFoundException()

        return ArticleDto(articleWithBoard)
    }

    fun pageByBoardId(
        boardIds: List<Long>?,
        keyword: String?,
        nextPublishedAt: Instant?,
        nextId: Long?,
        limit: Int,
    ): ArticlePagingResponse {
        val keyword = keyword?.trim()?.takeIf { it.isNotEmpty() }

        val boardFilter = !boardIds.isNullOrEmpty()
        val boardIds =
            if (boardFilter) {
                boardIds
            } else {
                listOf(-1L)
            }

        val queryLimit = limit + 1

        val articleWithBoards =
            articleRepository.findByBoardIdsWithCursor(
                boardFilter,
                boardIds,
                keyword,
                nextPublishedAt,
                nextId,
                queryLimit,
            )

        val hasNext = articleWithBoards.size > limit
        val pageArticles = if (hasNext) articleWithBoards.subList(0, limit) else articleWithBoards

        val newNextPublishedAt = if (hasNext) pageArticles.last().publishedAt else null
        val newNextId = if (hasNext) pageArticles.last().id else null

        return ArticlePagingResponse(
            pageArticles.map { ArticleDto(it) },
            ArticlePaging(newNextPublishedAt?.toEpochMilli(), newNextId, hasNext),
        )
    }

    fun create(
        content: String,
        author: String,
        originLink: String?,
        title: String,
        boardId: Long,
        publishedAt: Instant?,
    ): ArticleDto {
        if (content.isBlank()) {
            throw ArticleBlankContentException()
        }
        if (author.isBlank()) {
            throw ArticleBlankAuthorException()
        }
        if (publishedAt == null) {
            throw ArticleBlankPublishedException()
        }
        if (originLink != null && originLink.isBlank()) {
            throw ArticleBlankOriginLinkException()
        }
        if (title.isBlank()) {
            throw ArticleBlankTitleException()
        }
        val board = boardRepository.findByIdOrNull(boardId) ?: throw BoardNotFoundException()

        val article =
            saveNewArticle(
                Article(
                    boardId = board.id!!,
                    content = content,
                    author = author,
                    originLink = originLink,
                    title = title,
                    publishedAt = publishedAt,
                ),
            )
        return ArticleDto(article, board)
    }

    fun update(
        articleId: Long,
        content: String?,
        author: String?,
        originLink: String?,
        title: String?,
        publishedAt: Instant?,
    ): ArticleDto {
        if (content?.isBlank() == true) {
            throw ArticleBlankContentException()
        }
        if (author?.isBlank() == true) {
            throw ArticleBlankAuthorException()
        }
        if (originLink?.isBlank() == true) {
            throw ArticleBlankOriginLinkException()
        }
        if (title?.isBlank() == true) {
            throw ArticleBlankTitleException()
        }
        val article = articleRepository.findByIdOrNull(articleId) ?: throw ArticleNotFoundException()
        content?.let { article.content = it }
        author?.let { article.author = it }
        originLink?.let { article.originLink = it }
        publishedAt?.let { article.publishedAt = it }
        title?.let { article.title = it }
        saveNewArticle(article)
        val articleWithBoard = articleRepository.findByIdWithBoard(articleId) ?: throw ArticleNotFoundException()
        return ArticleDto(articleWithBoard)
    }

    fun delete(articleId: Long) {
        val article = articleRepository.findByIdOrNull(articleId) ?: throw ArticleNotFoundException()
        articleRepository.delete(article)
    }

    fun saveNewArticle(article: Article): Article {
        val savedArticle = articleRepository.save(article)
        eventPublisher.publishEvent(ArticleCreatedEvent(savedArticle))
        return savedArticle
    }
}

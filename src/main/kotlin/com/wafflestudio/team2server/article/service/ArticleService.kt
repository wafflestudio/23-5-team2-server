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
import com.wafflestudio.team2server.article.repository.ArticleRepository
import com.wafflestudio.team2server.board.BoardNotFoundException
import com.wafflestudio.team2server.board.repository.BoardRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class ArticleService(
    private val articleRepository: ArticleRepository,
    private val boardRepository: BoardRepository,
) {
    fun get(articleId: Long): ArticleDto {
        val articleWithBoard =
            articleRepository.findByIdWithBoard(articleId)
                ?: throw ArticleNotFoundException()

        return ArticleDto(articleWithBoard)
    }

    fun pageByBoardId(
        boardId: Long,
        nextPublishedAt: Instant?,
        nextId: Long?,
        limit: Int,
    ): ArticlePagingResponse {
        val board = boardRepository.findByIdOrNull(boardId) ?: throw BoardNotFoundException()

        val queryLimit = limit + 1
        val articleWithBoards =
            articleRepository.findByBoardIdWithCursor(board.id!!, nextPublishedAt, nextId, queryLimit)
        val hasNext = articleWithBoards.size > limit
        val pageArticles = if (hasNext) articleWithBoards.subList(0, limit) else articleWithBoards
        val newNextPublishedAt = if (hasNext) pageArticles.last().publishedAt else null
        val newnextId = if (hasNext) pageArticles.last().id else null
        return ArticlePagingResponse(
            pageArticles.map { ArticleDto(it) },
            ArticlePaging(newNextPublishedAt?.toEpochMilli(), newnextId, hasNext),
        )
    }

    fun create(
        content: String,
        author: String,
        originLink: String,
        title: String,
        boardId: Long,
        publihedAt: Instant?,
    ): ArticleDto {
        if (content.isBlank()) {
            throw ArticleBlankContentException()
        }
        if (author.isBlank()) {
            throw ArticleBlankAuthorException()
        }
        if (publihedAt == null) {
            throw ArticleBlankPublishedException()
        }
        if (originLink.isBlank()) {
            throw ArticleBlankOriginLinkException()
        }
        if (title.isBlank()) {
            throw ArticleBlankTitleException()
        }
        val board = boardRepository.findByIdOrNull(boardId) ?: throw BoardNotFoundException()

        val article =
            articleRepository.save(
                Article(
                    boardId = board.id!!,
                    content = content,
                    author = author,
                    originLink = originLink,
                    title = title,
                    publishedAt = publihedAt,
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
        articleRepository.save(article)
        val articleWithBoard = articleRepository.findByIdWithBoard(articleId) ?: throw ArticleNotFoundException()
        return ArticleDto(articleWithBoard)
    }

    fun delete(articleId: Long) {
        val article = articleRepository.findByIdOrNull(articleId) ?: throw ArticleNotFoundException()
        articleRepository.delete(article)
        // 삭제시 기사 포린키로 갖고 있는 개체들 삭제 되었는지 확인 필요(나중에 개발시)
    }
}

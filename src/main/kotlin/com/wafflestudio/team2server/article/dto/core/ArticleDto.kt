package com.wafflestudio.team2server.article.dto.core

import com.wafflestudio.team2server.article.model.Article
import com.wafflestudio.team2server.article.model.ArticleWithBoard
import com.wafflestudio.team2server.board.dto.core.BoardDto
import com.wafflestudio.team2server.board.model.Board
import java.time.Instant

data class ArticleDto(
    val id: Long,
    val board: BoardDto,
    val content: String,
    val author: String,
    val originLink: String,
    val title: String,
    val publishedAt: Instant,
    val createdAt: Instant,
    val updatedAt: Instant,
) {
    constructor(article: Article, board: Board) : this(
        id = article.id!!,
        board = BoardDto(board),
        content = article.content,
        author = article.author,
        originLink = article.originLink,
        title = article.title,
        publishedAt = article.publishedAt,
        createdAt = article.createdAt!!,
        updatedAt = article.updatedAt!!,
    )

    constructor(articleWithBoard: ArticleWithBoard) : this(
        id = articleWithBoard.id,
        board =
            BoardDto(
                id = articleWithBoard.board!!.id,
                name = articleWithBoard.board.name,
                sourceUrl = articleWithBoard.board.sourceUrl,
            ),
        content = articleWithBoard.content,
        author = articleWithBoard.author,
        originLink = articleWithBoard.originLink,
        title = articleWithBoard.title,
        publishedAt = articleWithBoard.publishedAt,
        createdAt = articleWithBoard.createdAt,
        updatedAt = articleWithBoard.updatedAt,
    )
}

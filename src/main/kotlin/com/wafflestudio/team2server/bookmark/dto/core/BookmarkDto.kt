package com.wafflestudio.team2server.bookmark.dto.core

import com.wafflestudio.team2server.article.dto.core.ArticleDto
import com.wafflestudio.team2server.bookmark.model.BookmarkWithArticle
import java.time.Instant

data class BookmarkDto(
    val id: Long,
    val userId: Long,
    val article: ArticleDto,
    val createdAt: Instant,
) {
    constructor(bm: BookmarkWithArticle) : this(bm.id, bm.userId, ArticleDto(bm.article), bm.createdAt)
}

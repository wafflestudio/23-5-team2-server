package com.wafflestudio.team2server.article.dto.response

import com.wafflestudio.team2server.article.dto.ArticlePaging
import com.wafflestudio.team2server.article.dto.core.ArticleDto

data class ArticlePagingResponse(
    val data: List<ArticleDto>,
    val paging: ArticlePaging,
)

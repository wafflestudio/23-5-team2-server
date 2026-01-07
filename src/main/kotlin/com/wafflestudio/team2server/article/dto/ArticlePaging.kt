package com.wafflestudio.team2server.article.dto

data class ArticlePaging(
    val nextPublishedAt: Long?,
    val nextId: Long?,
    val hasNext: Boolean,
)

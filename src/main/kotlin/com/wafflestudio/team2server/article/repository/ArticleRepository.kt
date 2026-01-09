package com.wafflestudio.team2server.article.repository

import com.wafflestudio.team2server.article.model.Article
import org.springframework.data.repository.ListCrudRepository

interface ArticleRepository : ListCrudRepository<Article, Long> {
    fun existsByOriginLink(originLink: String): Boolean
}

package com.wafflestudio.team2server.article.model
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.relational.core.mapping.Table
import java.time.Instant

@Table("articles")
class Article(
    @Id
    val id: Long? = null,
    var boardId: Long,
    var content: String,
    var author: String,
    var title: String,
    var originLink: String?,
    var publishedAt: Instant,
    var views: Int = 0,
    @CreatedDate
    var createdAt: Instant? = null,
    @LastModifiedDate
    var updatedAt: Instant? = null,
)

// Define the event record
data class ArticleCreatedEvent(
    val article: Article,
)

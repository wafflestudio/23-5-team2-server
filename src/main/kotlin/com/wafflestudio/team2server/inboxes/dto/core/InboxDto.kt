package com.wafflestudio.team2server.inboxes.dto.core

import com.fasterxml.jackson.annotation.JsonProperty
import com.wafflestudio.team2server.article.dto.core.ArticleDto
import com.wafflestudio.team2server.inboxes.model.InboxWithArticle
import java.time.Instant

data class InboxDto(
    val id: Long,
    val userId: Long,
    val article: ArticleDto,
    @get:JsonProperty("isRead")
    val isRead: Boolean,
    val createdAt: Instant,
) {
    constructor(inbox: InboxWithArticle) : this(inbox.id, inbox.userId, ArticleDto(inbox.article), inbox.isRead, inbox.createdAt)
}

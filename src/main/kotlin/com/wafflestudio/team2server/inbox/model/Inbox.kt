package com.wafflestudio.team2server.inbox.model

import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.time.Instant

@Table("inboxes")
class Inbox(
    @Id val id: Long? = null,
    var userId: Long,
    var articleId: Long,
    var isRead: Boolean = false,
    @CreatedDate
    var createdAt: Instant? = null,
)

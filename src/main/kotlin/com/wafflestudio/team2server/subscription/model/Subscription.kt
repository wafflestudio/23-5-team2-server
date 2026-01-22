package com.wafflestudio.team2server.subscription.model

import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.time.Instant

@Table("subscriptions")
class Subscription(
    @Id val id: Long? = null,
    var userId: Long,
    var boardId: Long,
    @CreatedDate
    var createdAt: Instant? = null,
)

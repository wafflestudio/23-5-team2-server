package com.wafflestudio.team2server.subscription.dto.core

import com.wafflestudio.team2server.subscription.model.Subscription
import java.time.Instant

data class SubscriptionDto(
    val id: Long,
    val userId: Long,
    val boardId: Long,
    val createdAt: Instant,
) {
    constructor(sub: Subscription) : this(sub.id!!, sub.userId, sub.boardId, sub.createdAt!!)
}

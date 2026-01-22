package com.wafflestudio.team2server.subscription.repository

import com.wafflestudio.team2server.subscription.model.Subscription
import org.springframework.data.repository.ListCrudRepository

interface SubscriptionRepository : ListCrudRepository<Subscription, Long> {
    fun existsByUserIdAndBoardId(
        userId: Long,
        boardId: Long,
    ): Boolean

    fun existsByIdAndUserId(
        id: Long,
        userId: Long,
    ): Boolean

    fun findByUserId(userId: Long): List<Subscription>
}

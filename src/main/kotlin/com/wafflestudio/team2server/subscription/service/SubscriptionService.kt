package com.wafflestudio.team2server.subscription.service

import com.wafflestudio.team2server.subscription.DuplicateSubscriptionException
import com.wafflestudio.team2server.subscription.SubscriptionNotFoundException
import com.wafflestudio.team2server.subscription.dto.core.SubscriptionDto
import com.wafflestudio.team2server.subscription.model.Subscription
import com.wafflestudio.team2server.subscription.repository.SubscriptionRepository
import org.springframework.stereotype.Service

@Service
class SubscriptionService(
    private val subscriptionRepository: SubscriptionRepository,
) {
    fun createSubscription(
        userId: Long,
        boardId: Long,
    ): SubscriptionDto {
        if (subscriptionRepository.existsByUserIdAndBoardId(userId, boardId)) {
            throw DuplicateSubscriptionException()
        }
        val sub = subscriptionRepository.save(Subscription(userId = userId, boardId = boardId))
        return SubscriptionDto(sub)
    }

    fun getUserSubscriptions(userId: Long): List<SubscriptionDto> = subscriptionRepository.findByUserId(userId).map { SubscriptionDto(it) }

    fun deleteSubscription(
        userId: Long,
        subscriptionId: Long,
    ) {
        if (!subscriptionRepository.existsByIdAndUserId(subscriptionId, userId)) {
            throw SubscriptionNotFoundException()
        }
        subscriptionRepository.deleteById(subscriptionId)
    }
}

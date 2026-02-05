package com.wafflestudio.team2server.subscription.dto

import com.wafflestudio.team2server.subscription.dto.core.SubscriptionDto

data class SubscriptionsResponse(
    val subscriptions: List<SubscriptionDto>,
)

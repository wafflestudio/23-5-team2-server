package com.wafflestudio.team2server.article

import com.wafflestudio.team2server.article.model.ArticleCreatedEvent
import com.wafflestudio.team2server.inboxes.service.InboxService
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

@Component
class ArticleEventListener(
    private val inboxService: InboxService, // Assuming you have a service to handle inbox creation
) {
    @EventListener
    fun handleArticleCreated(event: ArticleCreatedEvent) {
        val article = event.article
        // Logic to find subscribers of article.boardId and create inboxes
        inboxService.createInboxesForBoardSubscribers(article)
    }
}

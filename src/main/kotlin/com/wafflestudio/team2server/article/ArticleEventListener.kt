package com.wafflestudio.team2server.article

import com.wafflestudio.team2server.article.model.ArticleCreatedEvent
import com.wafflestudio.team2server.email.service.EmailService
import com.wafflestudio.team2server.inboxes.service.InboxService
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class ArticleEventListener(
    private val inboxService: InboxService,
    private val emailService: EmailService, // Assuming you have a service to handle inbox creation
) {
    @EventListener
    @Transactional
    fun handleArticleCreated(event: ArticleCreatedEvent) {
        val article = event.article
        // Logic to find subscribers of article.boardId and create inboxes
        inboxService.createInboxesForBoardSubscribers(article)
    }

    fun sendEmails(event: ArticleCreatedEvent) {
        val article = event.article
        val title = article.title
        val content = article.content

        val recipients: List<String> = emailService.getSubscriberEmails(article.boardId)

        if (recipients.isEmpty()) return
    }
}

package com.wafflestudio.team2server.article

import com.wafflestudio.team2server.article.model.ArticleCreatedEvent
import com.wafflestudio.team2server.email.service.EmailService
import com.wafflestudio.team2server.email.service.MailService
import com.wafflestudio.team2server.inbox.service.InboxService
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class ArticleEventListener(
    private val inboxService: InboxService,
    private val emailService: EmailService,
    private val mailService: MailService,
) {
    @EventListener
    @Transactional
    fun handleArticleCreated(event: ArticleCreatedEvent) {
        val article = event.article

        val recipients = emailService.getSubscriberEmails(article.boardId)

        recipients.forEach { email ->
            mailService.sendArticleNotification(email, article)
        }

        inboxService.createInboxesForBoardSubscribers(article)
    }
}

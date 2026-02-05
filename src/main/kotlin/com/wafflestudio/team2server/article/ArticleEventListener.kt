package com.wafflestudio.team2server.article

import com.wafflestudio.team2server.article.model.ArticleCreatedEvent
import com.wafflestudio.team2server.email.service.EmailService
import com.wafflestudio.team2server.email.service.MailService
import com.wafflestudio.team2server.inbox.service.InboxService
import org.slf4j.LoggerFactory
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class ArticleEventListener(
    private val inboxService: InboxService,
    private val emailService: EmailService,
    private val mailService: MailService,
) {
    private val logger = LoggerFactory.getLogger(ArticleEventListener::class.java)

    @EventListener
    @Transactional
    fun handleArticleCreated(event: ArticleCreatedEvent) {
        val article = event.article
        logger.info("게시글 생성 이벤트 수신: 게시글ID={}, 제목={}, 게시판ID={}", article.id, article.title, article.boardId)

        val recipients = emailService.getSubscriberEmails(article.boardId)
        logger.info("이메일 구독자 조회 완료: 게시판ID={}, 구독자 수={}", article.boardId, recipients.size)

        inboxService.createInboxesForBoardSubscribers(article)

        if (recipients.isEmpty()) {
            logger.debug("이메일 발송할 구독자가 없습니다: 게시판ID={}", article.boardId)
        } else {
            logger.info("이메일 발송 시작: 게시판ID={}, 구독자 수={}", article.boardId, recipients.size)
            recipients.forEach { email ->
                mailService.sendArticleNotification(email, article)
            }
        }
    }
}

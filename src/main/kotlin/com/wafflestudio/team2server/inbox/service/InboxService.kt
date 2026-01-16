package com.wafflestudio.team2server.inbox.service

import com.wafflestudio.team2server.article.model.Article
import com.wafflestudio.team2server.inbox.InboxNotFoundException
import com.wafflestudio.team2server.inbox.dto.InboxPaging
import com.wafflestudio.team2server.inbox.dto.InboxPagingResponse
import com.wafflestudio.team2server.inbox.dto.core.InboxDto
import com.wafflestudio.team2server.inbox.repository.InboxRepository
import org.springframework.stereotype.Service

@Service
class InboxService(
    private val inboxRepository: InboxRepository,
) {
    fun getInboxPaging(
        userId: Long,
        nextId: Long?,
        limit: Int,
    ): InboxPagingResponse {
        val inboxes = inboxRepository.findByUserIdWithCursor(userId, nextId, limit + 1)
        val hasNext = inboxes.size > limit
        val pageInboxes = if (hasNext) inboxes.subList(0, limit) else inboxes
        val newNextId = if (hasNext) pageInboxes.last().id else null
        return InboxPagingResponse(
            pageInboxes.map { InboxDto(it) },
            InboxPaging(newNextId, hasNext),
        )
    }

    fun markInboxAsRead(
        userId: Long,
        inboxId: Long,
    ) {
        val inbox = inboxRepository.findByIdAndUserId(inboxId, userId) ?: throw InboxNotFoundException()
        inbox.isRead = true
        inboxRepository.save(inbox)
    }

    fun deleteInbox(
        userId: Long,
        inboxId: Long,
    ) {
        val inbox = inboxRepository.findByIdAndUserId(inboxId, userId) ?: throw InboxNotFoundException()
        inboxRepository.delete(inbox)
    }

    fun createInboxesForBoardSubscribers(article: Article): Int =
        inboxRepository.createInboxesForBoardSubscribers(article.id!!, article.boardId)
}

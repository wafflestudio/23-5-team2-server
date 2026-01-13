package com.wafflestudio.team2server.inboxes.service

import com.wafflestudio.team2server.inboxes.InboxNotFoundException
import com.wafflestudio.team2server.inboxes.dto.InboxPaging
import com.wafflestudio.team2server.inboxes.dto.InboxPagingResponse
import com.wafflestudio.team2server.inboxes.dto.core.InboxDto
import com.wafflestudio.team2server.inboxes.repository.InboxRepository
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
}

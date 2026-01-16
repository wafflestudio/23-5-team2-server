package com.wafflestudio.team2server.inbox.dto

import com.wafflestudio.team2server.inbox.dto.core.InboxDto

data class InboxPagingResponse(
    val inboxes: List<InboxDto>,
    val paging: InboxPaging,
)

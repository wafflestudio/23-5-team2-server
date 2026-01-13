package com.wafflestudio.team2server.inboxes.dto

import com.wafflestudio.team2server.inboxes.dto.core.InboxDto

data class InboxPagingResponse(
    val inboxes: List<InboxDto>,
    val paging: InboxPaging,
)

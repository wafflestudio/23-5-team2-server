package com.wafflestudio.team2server.bookmark.dto

import com.wafflestudio.team2server.bookmark.dto.core.BookmarkDto

data class BookmarkPagingResponse(
    val bookmarks: List<BookmarkDto>,
    val paging: BookmarkPaging,
)

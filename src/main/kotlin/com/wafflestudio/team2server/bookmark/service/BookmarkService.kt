package com.wafflestudio.team2server.bookmark.service

import com.wafflestudio.team2server.bookmark.dto.BookmarkPaging
import com.wafflestudio.team2server.bookmark.dto.BookmarkPagingResponse
import com.wafflestudio.team2server.bookmark.dto.core.BookmarkDto
import com.wafflestudio.team2server.bookmark.repository.BookmarkRepository
import org.springframework.stereotype.Service

@Service
class BookmarkService(
    private val bookmarkRepository: BookmarkRepository,
) {
    fun getBookmarkPaging(
        userId: Long,
        nextId: Long?,
        limit: Int,
    ): BookmarkPagingResponse {
        val bookmarks = bookmarkRepository.findByUserIdWithCursor(userId, nextId, limit + 1)
        val hasNext = bookmarks.size > limit
        val pageBookmarks = if (hasNext) bookmarks.subList(0, limit) else bookmarks
        val newNextId = if (hasNext) pageBookmarks.last().id else null
        return BookmarkPagingResponse(
            pageBookmarks.map { BookmarkDto(it) },
            BookmarkPaging(newNextId, hasNext),
        )
    }

    fun deleteBookmarkIfExist(
        userId: Long,
        bookmarkId: Long,
    ): Int {
        if (bookmarkRepository.existsByIdAndUserId(bookmarkId, userId)) {
            bookmarkRepository.deleteById(bookmarkId)
            return 1
        }
        return 0
    }

    fun createBookmarkIfNotExist(
        userId: Long,
        articleId: Long,
    ): Int = bookmarkRepository.insertOrIgnore(userId, articleId)
}

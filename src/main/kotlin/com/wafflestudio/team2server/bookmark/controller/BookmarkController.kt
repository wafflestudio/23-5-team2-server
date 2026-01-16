package com.wafflestudio.team2server.bookmark.controller

import com.wafflestudio.team2server.bookmark.dto.BookmarkPagingResponse
import com.wafflestudio.team2server.bookmark.dto.CountResponse
import com.wafflestudio.team2server.bookmark.dto.CreateBookmarkRequest
import com.wafflestudio.team2server.bookmark.service.BookmarkService
import com.wafflestudio.team2server.user.LoggedInUser
import com.wafflestudio.team2server.user.model.User
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/bookmarks")
@Tag(name = "Bookmark", description = "북마크(저장한 게시글) 관리 API")
class BookmarkController(
    private val bookmarkService: BookmarkService,
) {
    @Operation(summary = "저장한 게시글 추가", description = "게시글을 저장한 게시글에 추가합니다. 추가된 개수를 반환합니다.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "201", description = "저장한 게시글 추가 성공"),
            ApiResponse(responseCode = "401", description = "사용자 인증 실패 (유효하지 않은 토큰)"),
        ],
    )
    @PostMapping
    fun createBookmark(
        @Parameter(hidden = true) @LoggedInUser user: User,
        @RequestBody body: CreateBookmarkRequest,
    ): ResponseEntity<CountResponse> {
        val cnt = bookmarkService.createBookmarkIfNotExist(user.id!!, body.articleId)
        return ResponseEntity.status(HttpStatus.CREATED).body(CountResponse(cnt))
    }

    @Operation(summary = "저장한 게시글 조회", description = "저장한 게시글을 페이지네이션합니다.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "게시글 조회 성공"),
            ApiResponse(responseCode = "401", description = "사용자 인증 실패 (유효하지 않은 토큰)"),
        ],
    )
    @GetMapping
    fun getBookmarks(
        @Parameter(hidden = true) @LoggedInUser user: User,
        @Parameter(
            description = "다음 페이지 커서 - 이전 응답의 마지막 게시글 ID",
        ) @RequestParam(value = "nextId", required = false) nextId: Long?,
        @Parameter(
            description = "페이지당 게시글 수",
            example = "20",
        ) @RequestParam(value = "limit", defaultValue = "20") limit: Int,
    ): ResponseEntity<BookmarkPagingResponse> = ResponseEntity.ok(bookmarkService.getBookmarkPaging(user.id!!, nextId, limit))

    @Operation(summary = "저장한 게시글 삭제", description = "저장한 게시글을 id로 삭제합니다. 삭제된 개수를 반환합니다.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "저장한 게시글 삭제 성공"),
            ApiResponse(responseCode = "401", description = "사용자 인증 실패 (유효하지 않은 토큰)"),
        ],
    )
    @DeleteMapping("/{id}")
    fun deleteBookmark(
        @Parameter(hidden = true) @LoggedInUser user: User,
        @Parameter(hidden = true) @PathVariable id: Long,
    ): ResponseEntity<CountResponse> {
        val cnt = bookmarkService.deleteBookmarkIfExist(user.id!!, id)
        return ResponseEntity.ok(CountResponse(cnt))
    }
}

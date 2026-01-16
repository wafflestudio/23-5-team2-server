package com.wafflestudio.team2server.inbox.controller

import com.wafflestudio.team2server.inbox.dto.InboxPagingResponse
import com.wafflestudio.team2server.inbox.service.InboxService
import com.wafflestudio.team2server.user.LoggedInUser
import com.wafflestudio.team2server.user.model.User
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/inboxes")
@Tag(name = "Inbox", description = "받은 게시글 관리 API")
class InboxController(
    private val inboxService: InboxService,
) {
    @Operation(summary = "받은 게시글 조회", description = "받은 게시글을 페이지네이션합니다.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "게시글 조회 성공"),
            ApiResponse(responseCode = "401", description = "사용자 인증 실패 (유효하지 않은 토큰)"),
        ],
    )
    @GetMapping
    fun getInboxes(
        @Parameter(hidden = true) @LoggedInUser user: User,
        @Parameter(
            description = "다음 페이지 커서 - 이전 응답의 마지막 게시글 ID",
        ) @RequestParam(value = "nextId", required = false) nextId: Long?,
        @Parameter(
            description = "페이지당 게시글 수",
            example = "20",
        ) @RequestParam(value = "limit", defaultValue = "20") limit: Int,
    ): ResponseEntity<InboxPagingResponse> = ResponseEntity.ok(inboxService.getInboxPaging(user.id!!, nextId, limit))

    @Operation(summary = "받은 게시글 읽음 표시", description = "특정 받은 게시글을 읽음 표시합니다.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "204", description = "받은 게시글 읽음 표시 성공"),
            ApiResponse(responseCode = "401", description = "사용자 인증 실패 (유효하지 않은 토큰)"),
            ApiResponse(responseCode = "404", description = "사용자의 받은 게시글 목록에 해당 받은 게시글 기록이 없음"),
        ],
    )
    @PatchMapping("/{id}")
    fun markInboxAsRead(
        @Parameter(hidden = true) @LoggedInUser user: User,
        @Parameter(hidden = true) @PathVariable id: Long,
    ): ResponseEntity<Unit> {
        inboxService.markInboxAsRead(user.id!!, id)
        return ResponseEntity.noContent().build()
    }

    @Operation(summary = "받은 게시글 삭제", description = "받은 게시글을 id로 삭제합니다.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "204", description = "받은 게시글 삭제 성공"),
            ApiResponse(responseCode = "401", description = "사용자 인증 실패 (유효하지 않은 토큰)"),
            ApiResponse(responseCode = "404", description = "사용자의 받은 게시글 목록에 해당 받은 게시글 기록이 없음"),
        ],
    )
    @DeleteMapping("/{id}")
    fun deleteInbox(
        @Parameter(hidden = true) @LoggedInUser user: User,
        @Parameter(hidden = true) @PathVariable id: Long,
    ): ResponseEntity<Unit> {
        inboxService.deleteInbox(user.id!!, id)
        return ResponseEntity.noContent().build()
    }
}

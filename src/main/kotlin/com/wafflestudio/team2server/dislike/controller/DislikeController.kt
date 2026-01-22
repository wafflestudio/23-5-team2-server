package com.wafflestudio.team2server.dislike.controller

import com.wafflestudio.team2server.dislike.dto.CreateDislikeRequest
import com.wafflestudio.team2server.dislike.dto.DislikesResponse
import com.wafflestudio.team2server.dislike.service.DislikeService
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
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/dislikes")
@Tag(name = "Dislike", description = "게시글 싫어요 관리 API")
class DislikeController(
    private val dislikeService: DislikeService,
) {
    @Operation(summary = "게시글 싫어요 추가", description = "게시글에 싫어요를 추가합니다. 최종 싫어요 개수를 반환합니다.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "201", description = "게시글 싫어요 추가 성공"),
            ApiResponse(responseCode = "401", description = "사용자 인증 실패 (유효하지 않은 토큰)"),
        ],
    )
    @PostMapping
    fun createDislike(
        @Parameter(hidden = true) @LoggedInUser user: User,
        @RequestBody body: CreateDislikeRequest,
    ): ResponseEntity<DislikesResponse> {
        val cnt = dislikeService.createDislike(user.id!!, body.articleId)
        return ResponseEntity.status(HttpStatus.CREATED).body(DislikesResponse(cnt))
    }

    @Operation(summary = "게시글 싫어요 기록 조회", description = "특정 게시글에 대한 사용자의 싫어요 기록 존재 여부를 조회합니다. ")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "204", description = "싫어요 기록 존재"),
            ApiResponse(responseCode = "401", description = "사용자 인증 실패 (유효하지 않은 토큰)"),
            ApiResponse(responseCode = "404", description = "싫어요 기록 없음"),
        ],
    )
    @GetMapping("/{articleId}")
    fun getDislike(
        @Parameter(hidden = true) @LoggedInUser user: User,
        @Parameter(
            description = "게시글 ID",
        ) @PathVariable articleId: Long,
    ): ResponseEntity<Unit> =
        if (dislikeService.existsByArticleIdAndUserId(
                articleId,
                user.id!!,
            )
        ) {
            ResponseEntity.noContent().build()
        } else {
            ResponseEntity.notFound().build()
        }

    @Operation(summary = "게시글 싫어요 삭제", description = "게시글 싫어요를 게시글 id로 삭제합니다. 최종 싫어요 개수를 반환합니다.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "저장한 게시글 삭제 성공"),
            ApiResponse(responseCode = "401", description = "사용자 인증 실패 (유효하지 않은 토큰)"),
        ],
    )
    @DeleteMapping("/{articleId}")
    fun deleteDislike(
        @Parameter(hidden = true) @LoggedInUser user: User,
        @Parameter(hidden = true) @PathVariable articleId: Long,
    ): ResponseEntity<DislikesResponse> {
        val cnt = dislikeService.deleteDislike(user.id!!, articleId)
        return ResponseEntity.ok(DislikesResponse(cnt))
    }
}

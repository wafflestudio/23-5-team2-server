package com.wafflestudio.team2server.subscription.controller

import com.wafflestudio.team2server.subscription.dto.CreateSubscriptionRequest
import com.wafflestudio.team2server.subscription.dto.DeleteSubscriptionRequest
import com.wafflestudio.team2server.subscription.dto.SubscriptionsResponse
import com.wafflestudio.team2server.subscription.dto.core.SubscriptionDto
import com.wafflestudio.team2server.subscription.service.SubscriptionService
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
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/subscriptions")
@Tag(name = "Subscription", description = "게시판 구독 관리 API")
class SubscriptionController(
    private val subscriptionService: SubscriptionService,
) {
    @Operation(summary = "게시판 구독 추가", description = "게시판 구독을 추가하고 그 기록을 반환합니다.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "201", description = "이미지 업로드 성공"),
            ApiResponse(responseCode = "401", description = "사용자 인증 실패 (유효하지 않은 토큰)"),
            ApiResponse(responseCode = "409", description = "중복된 구독"),
        ],
    )
    @PostMapping
    fun createSubscription(
        @Parameter(hidden = true) @LoggedInUser user: User,
        @RequestBody body: CreateSubscriptionRequest,
    ): ResponseEntity<SubscriptionDto> {
        val sub = subscriptionService.createSubscription(user.id!!, body.boardId)
        return ResponseEntity.status(HttpStatus.CREATED).body(sub)
    }

    @Operation(summary = "게시판 구독 목록 조회", description = "사용자의 게시판 구독 목록을 조회합니다.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "이미지 업로드 성공"),
            ApiResponse(responseCode = "401", description = "사용자 인증 실패 (유효하지 않은 토큰)"),
        ],
    )
    @GetMapping
    fun getSubscriptions(
        @Parameter(hidden = true) @LoggedInUser user: User,
    ): ResponseEntity<SubscriptionsResponse> = ResponseEntity.ok(SubscriptionsResponse(subscriptionService.getUserSubscriptions(user.id!!)))

    @Operation(summary = "게시판 구독 목록 삭제", description = "게시판 구독 목록을 id로 삭제합니다.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "204", description = "게시판 구독 목록 삭제 성공"),
            ApiResponse(responseCode = "401", description = "사용자 인증 실패 (유효하지 않은 토큰)"),
            ApiResponse(responseCode = "404", description = "사용자의 구독 목록에 해당 구독 기록이 없음"),
        ],
    )
    @DeleteMapping
    fun deleteSubscription(
        @Parameter(hidden = true) @LoggedInUser user: User,
        @RequestBody body: DeleteSubscriptionRequest,
    ): ResponseEntity<Unit> {
        subscriptionService.deleteSubscription(user.id!!, body.subscriptionId)
        return ResponseEntity.noContent().build()
    }
}

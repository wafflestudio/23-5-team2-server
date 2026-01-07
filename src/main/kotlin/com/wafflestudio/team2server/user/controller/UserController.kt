package com.wafflestudio.team2server.user.controller
import com.wafflestudio.team2server.user.LoggedInUser
import com.wafflestudio.team2server.user.dto.UpdateLocalRequest
import com.wafflestudio.team2server.user.dto.core.UserDto
import com.wafflestudio.team2server.user.model.User
import com.wafflestudio.team2server.user.service.UserService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/users")
@Tag(name = "User", description = "사용자 API")
class UserController(
    private val userService: UserService,
) {
    @Operation(summary = "본인 정보 조회", description = "로그인한 사용자의 정보를 조회합니다")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "사용자 정보 조회 성공"),
            ApiResponse(responseCode = "401", description = "인증 실패 (유효하지 않은 토큰)"),
        ],
    )
    @GetMapping("/me")
    fun me(
        @Parameter(hidden = true) @LoggedInUser user: User,
    ): ResponseEntity<UserDto> = ResponseEntity.ok(UserDto(user))

    @PatchMapping("/me/local")
    fun updateLocal(
        @Parameter(hidden = true) @LoggedInUser user: User,
        @RequestBody updateLocalRequest: UpdateLocalRequest,
    ): ResponseEntity<Unit> {
        val (oldPassword, newPassword) = updateLocalRequest
        userService.updateLocal(user, oldPassword, newPassword)
        return ResponseEntity.noContent().build()
    }

    @DeleteMapping("/me")
    fun deleteUser(
        @Parameter(hidden = true) @LoggedInUser user: User,
    ): ResponseEntity<Unit> {
        userService.deleteUser(user)
        return ResponseEntity.noContent().build()
    }
}

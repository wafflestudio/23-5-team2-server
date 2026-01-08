package com.wafflestudio.team2server.user.controller

import com.wafflestudio.team2server.user.JwtProvider
import com.wafflestudio.team2server.user.dto.LocalLoginRequest
import com.wafflestudio.team2server.user.dto.LocalRegisterRequest
import com.wafflestudio.team2server.user.dto.LocalRegisterResponse
import com.wafflestudio.team2server.user.service.UserService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseCookie
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Auth", description = "인증 API")
class AuthController(
    private val userService: UserService,
    private val jwtProvider: JwtProvider,
) {
    @Operation(summary = "회원가입", description = "새로운 사용자를 등록합니다")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "201", description = "회원가입 성공"),
            ApiResponse(responseCode = "400", description = "잘못된 요청 (id가 4자 미만 또는 password가 8자 미만)"),
            ApiResponse(responseCode = "409", description = "이미 존재하는 id"),
        ],
    )
    @PostMapping("/register/local")
    fun register(
        @RequestBody registerRequest: LocalRegisterRequest,
    ): ResponseEntity<LocalRegisterResponse> {
        val userDto =
            userService.registerLocal(
                localId = registerRequest.userId,
                password = registerRequest.password,
            )
        return ResponseEntity.status(HttpStatus.CREATED).body(userDto)
    }

    @Operation(summary = "로그인", description = "id와 password로 로그인하여 JWT 토큰을 쿠키에 설정합니다")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "로그인 성공, JWT 토큰 설정"),
            ApiResponse(responseCode = "401", description = "인증 실패 (id 또는 password 불일치)"),
        ],
    )
    @PostMapping("/login/local")
    fun login(
        @RequestBody loginRequest: LocalLoginRequest,
    ): ResponseEntity<Unit> {
        val token =
            userService.loginLocal(
                localId = loginRequest.userId,
                password = loginRequest.password,
            )
        val cookie = jwtProvider.createJwtCookie(token)
        val headers = HttpHeaders()
        headers.add(HttpHeaders.SET_COOKIE, cookie.toString())
        return ResponseEntity.ok().headers(headers).build()
    }

    /**
     * This clears `AUTH-TOKEN` cookie. This requires no authenticated user to improve performance.
     */
    @Operation(summary = "로그아웃", description = "JWT 토큰을 저장하는 쿠키를 초기화합니다")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "로그아웃 성공, AUTH-TOKEN 쿠키 초기화"),
        ],
    )
    @PostMapping("/logout")
    fun logout(): ResponseEntity<Unit> {
        val cookie =
            ResponseCookie
                .from("AUTH-TOKEN", "")
                .httpOnly(true)
                .path("/")
                .maxAge(0)
                .build()
        val headers = HttpHeaders()
        headers.add(HttpHeaders.SET_COOKIE, cookie.toString())
        return ResponseEntity.ok().headers(headers).build()
    }
}

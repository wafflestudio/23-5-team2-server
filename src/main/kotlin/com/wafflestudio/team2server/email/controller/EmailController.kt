package com.wafflestudio.team2server.email.controller

import com.wafflestudio.team2server.email.dto.EmailRequestAndResponse
import com.wafflestudio.team2server.email.service.EmailService
import com.wafflestudio.team2server.user.LoggedInUser
import com.wafflestudio.team2server.user.model.User
import io.swagger.v3.oas.annotations.Parameter
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/emails")
class EmailController(
    private val emailService: EmailService,
) {
    @PostMapping
    fun registerEmail(
        @Parameter(hidden = true) @LoggedInUser user: User,
        @RequestBody request: EmailRequestAndResponse,
    ): ResponseEntity<Unit> {
        emailService.registerEmail(user.id!!, request.email)
        return ResponseEntity.ok().build()
    }

    @GetMapping
    fun getMyEmail(
        @Parameter(hidden = true) @LoggedInUser user: User,
    ): ResponseEntity<EmailRequestAndResponse> {
        val email =
            emailService.getMyEmail(user.id!!)
                ?: throw IllegalArgumentException("등록된 이메일이 없습니다.")

        return ResponseEntity.ok(EmailRequestAndResponse(email))
    }

    @DeleteMapping
    fun deleteEmail(
        @Parameter(hidden = true) @LoggedInUser user: User,
    ): ResponseEntity<Unit> {
        emailService.deleteEmail(user.id!!)
        return ResponseEntity.noContent().build()
    }
}

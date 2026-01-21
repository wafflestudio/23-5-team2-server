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
@RequestMapping("/api/v1//emails")
class EmailController(
    private val emailService: EmailService,
) {
    @PostMapping
    fun addEmail(
        @Parameter(hidden = true) @LoggedInUser user: User,
        @RequestBody request: EmailRequestAndResponse,
    ): ResponseEntity<Unit> {
        emailService.addEmail(user.id!!, request.email)
        return ResponseEntity.ok().build()
    }

    @GetMapping
    fun getMyEmails(
        @Parameter(hidden = true) @LoggedInUser user: User,
    ): ResponseEntity<List<EmailRequestAndResponse>> {
        val emails = emailService.getMyEmails(user.id!!)
        val response = emails.map { EmailRequestAndResponse(it.email) }
        return ResponseEntity.ok(response)
    }

    @DeleteMapping
    fun deleteEmail(
        @Parameter(hidden = true) @LoggedInUser user: User,
        @RequestBody request: EmailRequestAndResponse,
    ): ResponseEntity<Unit> {
        emailService.deleteEmail(user.id!!, request.email)
        return ResponseEntity.noContent().build()
    }
}

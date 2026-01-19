package com.wafflestudio.team2server.email.service

import com.wafflestudio.team2server.email.model.Email
import com.wafflestudio.team2server.email.repository.EmailRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Service
class EmailService(
    private val emailRepository: EmailRepository,
) {
    @Transactional
    fun registerEmail(
        userId: Long,
        emailAddress: String,
    ) {
        if (emailRepository.existsByEmail(emailAddress)) {
            throw IllegalArgumentException("이미 등록된 이메일 입니다.")
        }

        val existingEmail = emailRepository.findByUserId(userId)
        if (existingEmail != null) {
            emailRepository.delete(existingEmail)
        }

        emailRepository.save(Email(userId = userId, email = emailAddress, createdAt = Instant.now(),))
    }

    fun getMyEmail(userId: Long): String? = emailRepository.findByUserId(userId)?.email

    @Transactional
    fun deleteEmail(userId: Long) {
        val email =
            emailRepository.findByUserId(userId)
                ?: throw IllegalArgumentException("이메일이 없습니다.")
        emailRepository.delete(email)
    }

    fun getSubscriberEmails(boardId: Long): List<String> = emailRepository.findEmailsByBoardId(boardId)
}

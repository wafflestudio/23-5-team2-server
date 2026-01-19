package com.wafflestudio.team2server.email.service

import com.wafflestudio.team2server.email.EmailAlreadyExistsException
import com.wafflestudio.team2server.email.EmailNotFoundException
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
    fun addEmail(
        userId: Long,
        emailAddress: String,
    ) {
        if (emailRepository.existsByEmail(emailAddress)) {
            throw EmailAlreadyExistsException()
        }
        emailRepository.save(Email(userId = userId, email = emailAddress, createdAt = Instant.now()))
    }

    fun getMyEmails(userId: Long): List<Email> = emailRepository.findAllByUserId(userId)

    @Transactional
    fun deleteEmail(
        userId: Long,
        emailAddress: String,
    ) {
        val email =
            emailRepository.findByUserIdAndEmail(userId, emailAddress) ?: throw EmailNotFoundException()
        emailRepository.delete(email)
    }

    fun getSubscriberEmails(boardId: Long): List<String> = emailRepository.findEmailsByBoardId(boardId)
}

package com.wafflestudio.team2server.email.repository

import com.wafflestudio.team2server.email.model.Email
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.ListCrudRepository
import org.springframework.data.repository.query.Param

interface EmailRepository : ListCrudRepository<Email, Long> {
    fun findAllByUserId(userId: Long): List<Email>

    fun existsByEmail(email: String): Boolean

    fun findByUserIdAndEmail(
        userId: Long,
        email: String,
    ): Email

    @Query(
        """
        SELECT e.email
        FROM emails e
        JOIN subscriptions s ON e.user_id = s.user_id
        WHERE s.board_id = :boardId
    """,
    )
    fun findEmailsByBoardId(
        @Param("boardId") boardId: Long,
    ): List<String>
}

package com.wafflestudio.team2server.email.model

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.time.Instant

@Table("emails")
data class Email(
    @Id
    val id: Long? = null,
    val userId: Long,
    val email: String,
    val createdAt: Instant? = null,
)

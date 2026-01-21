package com.wafflestudio.team2server.dislike.model

import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.time.Instant

@Table("dislikes")
class Dislike(
    @Id val id: Long? = null,
    var userId: Long,
    var articleId: Long,
    @CreatedDate
    var createdAt: Instant? = null,
)

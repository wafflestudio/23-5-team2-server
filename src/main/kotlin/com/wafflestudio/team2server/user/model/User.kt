package com.wafflestudio.team2server.user.model

import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.relational.core.mapping.Table
import java.time.Instant

@Table("users")
class User(
    @Id var id: Long? = null,
    var localId: String? = null,
    var password: String? = null,
    var oauthId: String? = null,
    var oauthProvider: String? = null,
    var role: Int = 0,
    @CreatedDate
    var createdAt: Instant? = null,
    @LastModifiedDate
    var updatedAt: Instant? = null,
)

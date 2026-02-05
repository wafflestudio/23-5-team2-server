package com.wafflestudio.team2server.user.dto.core

import com.wafflestudio.team2server.user.model.User
import java.time.Instant

data class UserDto(
    val id: Long,
    val localId: String?,
    val oauthId: String?,
    val oauthProvider: String?,
    val role: Int,
    val createdAt: Instant,
    val updatedAt: Instant,
) {
    constructor(user: User) : this(user.id!!, user.localId, user.oauthId, user.oauthProvider, user.role, user.createdAt!!, user.updatedAt!!)
}

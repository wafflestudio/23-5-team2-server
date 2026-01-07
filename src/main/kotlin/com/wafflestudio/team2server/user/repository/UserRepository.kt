package com.wafflestudio.team2server.user.repository

import com.wafflestudio.team2server.user.model.User
import org.springframework.data.repository.ListCrudRepository

interface UserRepository : ListCrudRepository<User, Long> {
    fun findByLocalId(localId: String): User?

    fun findByOauthId(oauthId: String): User?

    fun existsByLocalId(localId: String): Boolean

    fun existsByOAuthId(oauthId: String): Boolean
}

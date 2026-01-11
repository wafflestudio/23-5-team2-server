package com.wafflestudio.team2server.helper

import com.wafflestudio.team2server.user.JwtProvider
import com.wafflestudio.team2server.user.model.User
import com.wafflestudio.team2server.user.repository.UserRepository
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Component
import kotlin.random.Random

@Component
class DataGenerator(
    private val userRepository: UserRepository,
    private val jwtProvider: JwtProvider,
    private val bcryptPasswordEncoder: BCryptPasswordEncoder,
) {
    fun generateUser(
        userId: String? = null,
        password: String? = null,
    ): Pair<User, String> {
        val user =
            userRepository.save(
                User(
                    localId = userId ?: "user-${Random.nextInt(1000000)}",
                    password = bcryptPasswordEncoder.encode(password ?: "password-${Random.nextInt(1000000)}"),
                ),
            )
        return user to jwtProvider.createToken(user.id!!)
    }
}

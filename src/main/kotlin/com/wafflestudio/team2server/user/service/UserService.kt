package com.wafflestudio.team2server.user.service

import com.wafflestudio.team2server.user.AuthenticateException
import com.wafflestudio.team2server.user.ChangePasswordIllegalStateException
import com.wafflestudio.team2server.user.InvalidNewPasswordException
import com.wafflestudio.team2server.user.InvalidOldPasswordException
import com.wafflestudio.team2server.user.JwtProvider
import com.wafflestudio.team2server.user.SignUpBadLocalIdException
import com.wafflestudio.team2server.user.SignUpBadPasswordException
import com.wafflestudio.team2server.user.SignUpLocalIdConflictException
import com.wafflestudio.team2server.user.dto.core.UserDto
import com.wafflestudio.team2server.user.model.User
import com.wafflestudio.team2server.user.repository.UserRepository
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Service

@Service
class UserService(
    private val userRepository: UserRepository,
    private val jwtProvider: JwtProvider,
    private val bcryptPasswordEncoder: BCryptPasswordEncoder,
) {
    fun registerLocal(
        localId: String,
        password: String,
    ): UserDto {
        if (localId.length < 4) {
            throw SignUpBadLocalIdException()
        }
        if (password.length < 8) {
            throw SignUpBadPasswordException()
        }

        if (userRepository.existsByLocalId(localId)) {
            throw SignUpLocalIdConflictException()
        }

        val encryptedPassword = bcryptPasswordEncoder.encode(password)
        val user =
            userRepository.save(
                User(
                    localId = localId,
                    password = encryptedPassword,
                ),
            )
        return UserDto(user)
    }

    fun loginLocal(
        localId: String,
        password: String,
    ): String {
        val user = userRepository.findByLocalId(localId)
        if (bcryptPasswordEncoder.matches(password, user?.password).not()) {
            throw AuthenticateException()
        }
        val jwt = jwtProvider.createToken(user?.id!!)
        return jwt
    }

    fun updateLocal(
        user: User,
        oldPassword: String,
        newPassword: String,
    ): UserDto {
        if (user.oauthProvider != null) {
            throw ChangePasswordIllegalStateException()
        }
        if (newPassword.length < 8) {
            throw InvalidNewPasswordException()
        }
        if (!bcryptPasswordEncoder.matches(oldPassword, user.password)) {
            throw InvalidOldPasswordException()
        }
        if (oldPassword == newPassword) {
            throw InvalidNewPasswordException()
        }
        user.password = bcryptPasswordEncoder.encode(newPassword)
        userRepository.save(user)
        return UserDto(user)
    }

    fun deleteUser(user: User) {
        userRepository.delete(user)
    }
}

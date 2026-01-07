package com.wafflestudio.team2server.user.service

import com.wafflestudio.team2server.user.model.User
import com.wafflestudio.team2server.user.repository.UserRepository
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest
import org.springframework.security.oauth2.core.OAuth2AuthenticationException
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.stereotype.Service

@Service
class GoogleOAuth2UserService(
    private val userRepository: UserRepository,
) : DefaultOAuth2UserService() {
    @Throws(OAuth2AuthenticationException::class)
    override fun loadUser(userRequest: OAuth2UserRequest?): OAuth2User {
        val oAuth2User = super.loadUser(userRequest)

        // 1. Get email from Google attributes
        val email = oAuth2User.getAttribute<String?>("email")
        if (email == null || email.isEmpty()) {
            throw OAuth2AuthenticationException("Invalid email format")
        }

        // 2. Database Check & Create
        val user = userRepository.findByOauthId(email)
        if (user == null) {
            userRepository.save(User(oauthId = email, oauthProvider = "google"))
        }

        return oAuth2User
    }
}

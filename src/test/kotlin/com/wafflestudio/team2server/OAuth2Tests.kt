package com.wafflestudio.team2server

import com.wafflestudio.team2server.user.JwtProvider
import com.wafflestudio.team2server.user.OAuth2SuccessHandler
import com.wafflestudio.team2server.user.model.User
import com.wafflestudio.team2server.user.repository.UserRepository
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.ArgumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpHeaders
import org.springframework.security.core.Authentication
import org.springframework.security.oauth2.core.user.DefaultOAuth2User
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.test.context.ActiveProfiles
import org.testcontainers.junit.jupiter.Testcontainers

@SpringBootTest
@ActiveProfiles("test")
@Testcontainers
class OAuth2Tests
    @Autowired
    constructor(
        private val oAuth2SuccessHandler: OAuth2SuccessHandler,
        private val userRepository: UserRepository,
        private val jwtProvider: JwtProvider,
    ) {
        @Test
        fun `OAuth2SuccessHandler generates JWT token and redirects for new user`() {
            // Given: A new OAuth2 user that should be auto-registered
            val email = "oauth2newuser@gmail.com"
            userRepository.findByOauthId(email)?.let { userRepository.delete(it) }

            // Create user (simulating what GoogleOAuth2UserService does)
            val user =
                userRepository.save(
                    User(
                        oauthId = email,
                        oauthProvider = "google",
                    ),
                )

            val oAuth2User: OAuth2User =
                DefaultOAuth2User(
                    emptyList(),
                    mapOf("email" to email, "name" to "Test User"),
                    "email",
                )

            // Mock request, response, and authentication
            val request = mock<HttpServletRequest>()
            val response = mock<HttpServletResponse>()
            val authentication = mock<Authentication>()

            whenever(authentication.principal).thenReturn(oAuth2User)

            // When: onAuthenticationSuccess is called
            oAuth2SuccessHandler.onAuthenticationSuccess(request, response, authentication)

            // Then: Should add Set-Cookie header with JWT token
            val headerCaptor = ArgumentCaptor.forClass(String::class.java)
            val valueCaptor = ArgumentCaptor.forClass(String::class.java)

            verify(response).addHeader(headerCaptor.capture(), valueCaptor.capture())

            assertEquals(HttpHeaders.SET_COOKIE, headerCaptor.value)
            assertTrue(valueCaptor.value.contains("AUTH-TOKEN="))

            // Extract token from cookie string
            val cookieString = valueCaptor.value
            val tokenMatch = Regex("AUTH-TOKEN=([^;]+)").find(cookieString)
            assertNotNull(tokenMatch)

            val token = tokenMatch!!.groupValues[1]
            assertTrue(jwtProvider.validateToken(token))
            assertEquals(user.id, jwtProvider.getUserId(token))
        }

        @Test
        fun `OAuth2SuccessHandler generates JWT token for existing user`() {
            // Given: An existing OAuth2 user
            val email = "oauth2existinguser@gmail.com"
            userRepository.findByOauthId(email)?.let { userRepository.delete(it) }
            val user =
                userRepository.save(
                    User(
                        oauthId = email,
                        oauthProvider = "google",
                    ),
                )

            val oAuth2User: OAuth2User =
                DefaultOAuth2User(
                    emptyList(),
                    mapOf("email" to email, "name" to "Existing User"),
                    "email",
                )

            // Mock request, response, and authentication
            val request = mock<HttpServletRequest>()
            val response = mock<HttpServletResponse>()
            val authentication = mock<Authentication>()

            whenever(authentication.principal).thenReturn(oAuth2User)

            // When: onAuthenticationSuccess is called
            oAuth2SuccessHandler.onAuthenticationSuccess(request, response, authentication)

            // Then: Should add Set-Cookie header with JWT token
            val headerCaptor = ArgumentCaptor.forClass(String::class.java)
            val valueCaptor = ArgumentCaptor.forClass(String::class.java)

            verify(response).addHeader(headerCaptor.capture(), valueCaptor.capture())

            assertEquals(HttpHeaders.SET_COOKIE, headerCaptor.value)
            assertTrue(valueCaptor.value.contains("AUTH-TOKEN="))

            // Verify token is valid and contains correct user ID
            val cookieString = valueCaptor.value
            val tokenMatch = Regex("AUTH-TOKEN=([^;]+)").find(cookieString)
            assertNotNull(tokenMatch)

            val token = tokenMatch!!.groupValues[1]
            assertTrue(jwtProvider.validateToken(token))
            assertEquals(user.id, jwtProvider.getUserId(token))
        }

        @Test
        fun `OAuth2SuccessHandler throws exception when user not found`() {
            // Given: Non-existent user email
            val email = "nonexistent@gmail.com"

            // Ensure user doesn't exist
            userRepository.findByOauthId(email)?.let { userRepository.delete(it) }

            val oAuth2User: OAuth2User =
                DefaultOAuth2User(
                    emptyList(),
                    mapOf("email" to email, "name" to "Test User"),
                    "email",
                )

            val request = mock<HttpServletRequest>()
            val response = mock<HttpServletResponse>()
            val authentication = mock<Authentication>()

            whenever(authentication.principal).thenReturn(oAuth2User)

            // When & Then: Should throw IllegalStateException
            assertThrows(IllegalStateException::class.java) {
                oAuth2SuccessHandler.onAuthenticationSuccess(request, response, authentication)
            }
        }

        @Test
        fun `OAuth2SuccessHandler throws exception when email attribute is missing`() {
            // Given: OAuth2User without email attribute
            val oAuth2User: OAuth2User =
                DefaultOAuth2User(
                    emptyList(),
                    mapOf("name" to "Test User"),
                    "name",
                )

            val request = mock<HttpServletRequest>()
            val response = mock<HttpServletResponse>()
            val authentication = mock<Authentication>()

            whenever(authentication.principal).thenReturn(oAuth2User)

            // When & Then: Should throw IllegalStateException
            assertThrows(IllegalStateException::class.java) {
                oAuth2SuccessHandler.onAuthenticationSuccess(request, response, authentication)
            }
        }

        @Test
        fun `OAuth2 user auto-registration flow creates user with correct fields`() {
            // This test simulates what GoogleOAuth2UserService does
            // Given: A new user email
            val email = "autoregistertest@gmail.com"

            // Clean up any existing user
            userRepository.findByOauthId(email)?.let { userRepository.delete(it) }

            // When: User is created (as GoogleOAuth2UserService would do)
            val createdUser =
                userRepository.save(
                    User(
                        oauthId = email,
                        oauthProvider = "google",
                    ),
                )

            // Then: User should be created with correct fields
            assertNotNull(createdUser.id)
            assertEquals(email, createdUser.oauthId)
            assertEquals("google", createdUser.oauthProvider)
            assertEquals(null, createdUser.localId)
            assertEquals(null, createdUser.password)
            assertNotNull(createdUser.createdAt)
            assertNotNull(createdUser.updatedAt)
        }

        @Test
        fun `OAuth2 login flow does not create duplicate users`() {
            // This test verifies that checking existsByOauthId prevents duplicates
            // Given: An existing OAuth2 user
            val email = "duplicatetest@gmail.com"
            userRepository.findByOauthId(email)?.let { userRepository.delete(it) }

            // First login - user creation
            if (!userRepository.existsByOauthId(email)) {
                userRepository.save(
                    User(
                        oauthId = email,
                        oauthProvider = "google",
                    ),
                )
            }

            // Verify user exists
            assertTrue(userRepository.existsByOauthId(email))
            val userAfterFirstLogin = userRepository.findByOauthId(email)
            assertNotNull(userAfterFirstLogin)

            // Second login - should not create duplicate
            if (!userRepository.existsByOauthId(email)) {
                userRepository.save(
                    User(
                        oauthId = email,
                        oauthProvider = "google",
                    ),
                )
            }

            // Verify user still exists and is the same user
            assertTrue(userRepository.existsByOauthId(email))
            val userAfterSecondLogin = userRepository.findByOauthId(email)
            assertNotNull(userAfterSecondLogin)
            assertEquals(userAfterFirstLogin?.id, userAfterSecondLogin?.id)
        }
    }

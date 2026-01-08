package com.wafflestudio.team2server

import com.fasterxml.jackson.databind.ObjectMapper
import com.wafflestudio.team2server.helper.DataGenerator
import com.wafflestudio.team2server.user.dto.LocalLoginRequest
import com.wafflestudio.team2server.user.dto.LocalRegisterRequest
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post
import org.testcontainers.junit.jupiter.Testcontainers

@SpringBootTest
@ActiveProfiles("test")
@Testcontainers
@AutoConfigureMockMvc
class AuthTests
    @Autowired
    constructor(
        private val mvc: MockMvc,
        private val objectMapper: ObjectMapper,
        private val dataGenerator: DataGenerator,
    ) {
        @Test
        fun `should register successfully`() {
            // 회원가입할 수 있다
            val userId = "username1"
            val password = "qwer1234"

            val request = LocalRegisterRequest(userId, password)
            mvc
                .post("/api/v1/auth/register/local") {
                    contentType = MediaType.APPLICATION_JSON
                    content = objectMapper.writeValueAsString(request)
                }.andExpect {
                    status { isCreated() }
                }
        }

        @Test
        fun `login successfully`() {
            // 1. Given: The password variable
            val rawPassword = "fixed-password-1234"
            val (user, _) = dataGenerator.generateUser(password = rawPassword)

            val request = LocalLoginRequest(userId = user.localId!!, password = rawPassword)

            // When & Then
            mvc
                .post("/api/v1/auth/login/local") {
                    contentType = MediaType.APPLICATION_JSON
                    content = objectMapper.writeValueAsString(request)
                    accept = MediaType.APPLICATION_JSON
                }.andExpect {
                    status { isOk() }
                    header { exists("Set-Cookie") }
                    cookie { exists("AUTH-TOKEN") }
                }
        }

        @Test
        fun `login fails with wrong password`() {
            // 1. Given: Fix the password variable
            val rawPassword = "fixed-password-1234"
            val (user, _) = dataGenerator.generateUser(password = rawPassword)

            val request = LocalLoginRequest(userId = user.localId!!, password = rawPassword + "a")

            // When & Then
            mvc
                .post("/api/v1/auth/login/local") {
                    contentType = MediaType.APPLICATION_JSON
                    content = objectMapper.writeValueAsString(request)
                }.andExpect {
                    status {
                        isUnauthorized()
                    }
                }
        }

        @Test
        fun `logout returns 200 and clears cookie`() {
            // When & Then
            mvc.post("/api/v1/auth/logout").andExpect {
                status { isOk() }
                header { exists("Set-Cookie") }
                cookie { value("AUTH-TOKEN", "") }
            }
        }
    }

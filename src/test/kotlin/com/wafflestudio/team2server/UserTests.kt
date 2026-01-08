package com.wafflestudio.team2server

import com.fasterxml.jackson.databind.ObjectMapper
import com.wafflestudio.team2server.helper.DataGenerator
import com.wafflestudio.team2server.user.dto.LocalLoginRequest
import com.wafflestudio.team2server.user.dto.UpdateLocalRequest
import jakarta.servlet.http.Cookie
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.delete
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.patch
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.testcontainers.junit.jupiter.Testcontainers

@SpringBootTest
@ActiveProfiles("test")
@Testcontainers
@AutoConfigureMockMvc
class UserTests
    @Autowired
    constructor(
        private val mvc: MockMvc,
        private val mapper: ObjectMapper,
        private val dataGenerator: DataGenerator,
    ) {
        @Test
        fun `me returns 200`() {
            // Given
            val (user, token) = dataGenerator.generateUser()

            // When & Then
            mvc
                .get("/api/v1/users/me") {
                    cookie(Cookie("AUTH-TOKEN", token))
                }.andExpect {
                    status { isOk() }
                    jsonPath("$.id").value(user.id)
                }
        }

        @Test
        fun `updateLocal changes password and verifies with login`() {
            // 1. Given: 초기 유저 생성
            val oldPassword = "oldPassword"
            val newPassword = "new-password-1234"
            val (user, token) = dataGenerator.generateUser(password = oldPassword)

            // 2. When: 비밀번호 변경 요청 (204 No Content)
            val updateRequest = UpdateLocalRequest(oldPassword, newPassword)
            mvc
                .patch("/api/v1/users/me/local") {
                    cookie(Cookie("AUTH-TOKEN", token))
                    contentType = MediaType.APPLICATION_JSON
                    content = mapper.writeValueAsString(updateRequest)
                }.andExpect {
                    status { isNoContent() }
                }

            // 3. Then: 새 비밀번호로 로그인 시도 (200 OK)
            val loginRequest = LocalLoginRequest(userId = user.localId!!, password = newPassword)
            mvc
                .post("/api/v1/auth/login/local") {
                    contentType = MediaType.APPLICATION_JSON
                    content = mapper.writeValueAsString(loginRequest)
                }.andExpect {
                    status { isOk() }
                    cookie { exists("AUTH-TOKEN") }
                }
        }

        @Test
        fun `deleteUser removes user and verifies login fails`() {
            // 1. Given: 유저 생성
            val userId = "delete-user"
            val password = "password123"
            val (_, token) = dataGenerator.generateUser(userId = userId, password = password)

            // 2. When: 회원 탈퇴 요청 (204 No Content)
            mvc
                .delete("/api/v1/users/me") {
                    cookie(Cookie("AUTH-TOKEN", token))
                }.andExpect {
                    status { isNoContent() }
                }

            // 3. Then: 탈퇴한 계정으로 로그인 시도 (401 Unauthorized)
            val loginRequest = LocalLoginRequest(userId = userId, password = password)
            mvc
                .post("/api/v1/auth/login/local") {
                    contentType = MediaType.APPLICATION_JSON
                    content = mapper.writeValueAsString(loginRequest)
                }.andExpect {
                    status { isUnauthorized() }
                }
        }
    }

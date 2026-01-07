package com.wafflestudio.team2server

import com.fasterxml.jackson.databind.ObjectMapper
import com.wafflestudio.team2server.helper.DataGenerator
import com.wafflestudio.team2server.user.dto.LocalRegisterRequest
import jakarta.servlet.http.Cookie
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.header
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
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
        fun `should register successfully`() {
            // 회원가입할 수 있다
            val userId = "username1"
            val password = "qwer1234"

            val request = LocalRegisterRequest(userId, password)
            mvc
                .perform(
                    post("/api/v1/auth/register/local")
                        .content(mapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON),
                ).andExpect(status().isCreated)
        }

        @Test
        fun `me returns 200`() {
            // Given
            val (user, token) = dataGenerator.generateUser()

            // When & Then
            mvc
                .perform(
                    get("/api/v1/users/me")
                        .cookie(Cookie("AUTH-TOKEN", token)), // 인증용 쿠키 주입
                ).andExpect(status().isOk)
                .andExpect(jsonPath("$.id").value(user.id))
        }

        @Test
        fun `logout returns 200 and clears cookie`() {
            // Given
            val (_, token) = dataGenerator.generateUser()

            // When & Then
            mvc
                .perform(
                    post("/api/v1/users/logout")
                        .cookie(Cookie("access_token", token)),
                ).andExpect(status().isOk)
                .andExpect(header().exists("Set-Cookie"))
                // 쿠키 값이 비어있거나 만료 시간이 0인지 확인 가능
                .andExpect(cookie().value("AUTH-TOKEN", ""))
        }
    }

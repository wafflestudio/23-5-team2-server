package com.wafflestudio.team2server

import com.fasterxml.jackson.databind.ObjectMapper
import com.wafflestudio.team2server.email.dto.EmailRequestAndResponse
import com.wafflestudio.team2server.helper.DataGenerator
import jakarta.servlet.http.Cookie
import org.hamcrest.Matchers.hasSize
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.testcontainers.junit.jupiter.Testcontainers

@SpringBootTest
@ActiveProfiles("test")
@Testcontainers
@AutoConfigureMockMvc
class EmailTests
    @Autowired
    constructor(
        private val mvc: MockMvc,
        private val mapper: ObjectMapper,
        private val dataGenerator: DataGenerator,
    ) {
        @Test
        fun `addEmail returns 200 and saves email`() {
            val (_, token) = dataGenerator.generateUser()
            val request = EmailRequestAndResponse(email = "test@snu.ac.kr")

            mvc
                .perform(
                    post("/api/v1/emails")
                        .cookie(Cookie("AUTH-TOKEN", token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request)),
                ).andExpect(status().isOk)

            mvc
                .perform(
                    get("/api/v1/emails")
                        .cookie(Cookie("AUTH-TOKEN", token)),
                ).andExpect(status().isOk)
                .andExpect(jsonPath("$", hasSize<Any>(1)))
                .andExpect(jsonPath("$[0].email").value("test@snu.ac.kr"))
        }

        @Test
        fun `getMyEmails returns list of registered emails`() {
            val (user, token) = dataGenerator.generateUser()

            val email1 = EmailRequestAndResponse(email = "first@snu.ac.kr")
            val email2 = EmailRequestAndResponse(email = "second@snu.ac.kr")

            mvc
                .perform(
                    post("/api/v1/emails")
                        .cookie(Cookie("AUTH-TOKEN", token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(email1)),
                ).andExpect(status().isOk)

            mvc
                .perform(
                    post("/api/v1/emails")
                        .cookie(Cookie("AUTH-TOKEN", token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(email2)),
                ).andExpect(status().isOk)

            mvc
                .perform(
                    get("/api/v1/emails")
                        .cookie(Cookie("AUTH-TOKEN", token)),
                ).andExpect(status().isOk)
                .andExpect(jsonPath("$", hasSize<Any>(2)))
                .andExpect(jsonPath("$[0].email").value("first@snu.ac.kr"))
                .andExpect(jsonPath("$[1].email").value("second@snu.ac.kr"))
        }

        @Test
        fun `deleteEmail returns 204 and removes email`() {
            val (_, token) = dataGenerator.generateUser()
            val request = EmailRequestAndResponse(email = "delete-me@snu.ac.kr")

            mvc
                .perform(
                    post("/api/v1/emails")
                        .cookie(Cookie("AUTH-TOKEN", token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request)),
                ).andExpect(status().isOk)

            mvc
                .perform(
                    delete("/api/v1/emails")
                        .cookie(Cookie("AUTH-TOKEN", token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request)),
                ).andExpect(status().isNoContent) // 204 No Content

            mvc
                .perform(
                    get("/api/v1/emails")
                        .cookie(Cookie("AUTH-TOKEN", token)),
                ).andExpect(status().isOk)
                .andExpect(jsonPath("$", hasSize<Any>(0)))
        }
    }

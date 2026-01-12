package com.wafflestudio.team2server

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.testcontainers.junit.jupiter.Testcontainers

@SpringBootTest
@ActiveProfiles("test")
@Testcontainers
@AutoConfigureMockMvc
class BoardTests
    @Autowired
    constructor(
        private val mvc: MockMvc,
    ) {
        @Test
        fun `get boards returns ok and correct body structure`() {
            mvc.post("/api/v1/boards").andExpect {
                status { isOk() }
                jsonPath("$.boards").isArray
                jsonPath("$.boards[0].name").isString
            }
        }
    }

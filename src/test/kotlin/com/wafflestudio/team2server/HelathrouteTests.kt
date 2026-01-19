package com.wafflestudio.team2server

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.testcontainers.junit.jupiter.Testcontainers

@SpringBootTest
@ActiveProfiles("test")
@Testcontainers
@AutoConfigureMockMvc
class HelathrouteTests(
    @Autowired private val mvc: MockMvc,
) {
    @Test
    fun health_is_up() {
        // healthroute 정상작동한다
        mvc
            .perform(get("/actuator/health"))
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.status").value("UP"))
    }
}

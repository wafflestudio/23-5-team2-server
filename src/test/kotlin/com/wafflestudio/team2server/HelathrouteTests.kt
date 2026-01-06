package com.wafflestudio.team2server

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath

@SpringBootTest
@AutoConfigureMockMvc
class HelathrouteTests(
    @Autowired private val mockMvc: MockMvc
){
@Test
fun actuator_health_is_up() {
    mockMvc.perform(get("/actuator/health"))
        .andExpect(status().isOk)
        .andExpect(jsonPath("$.status").value("UP"))
}
}
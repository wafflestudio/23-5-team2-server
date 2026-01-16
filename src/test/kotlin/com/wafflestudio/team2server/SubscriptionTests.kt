package com.wafflestudio.team2server

import com.fasterxml.jackson.databind.ObjectMapper
import com.wafflestudio.team2server.helper.DataGenerator
import com.wafflestudio.team2server.subscription.dto.CreateSubscriptionRequest
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
class SubscriptionTests
    @Autowired
    constructor(
        private val mvc: MockMvc,
        private val mapper: ObjectMapper,
        private val dataGenerator: DataGenerator,
    ) {
        @Test
        fun `createSubscription returns 201 and adds subscription`() {
            // Given
            val (_, token) = dataGenerator.generateUser()
            val request = CreateSubscriptionRequest(boardId = 1L)

            // When & Then
            mvc
                .perform(
                    post("/api/v1/subscriptions")
                        .cookie(Cookie("AUTH-TOKEN", token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request)),
                ).andExpect(status().isCreated)
                .andExpect(jsonPath("$.boardId").value(1L))
        }

        @Test
        fun `getSubscriptions returns user's subscription list`() {
            // Given
            val (user, token) = dataGenerator.generateUser()

            dataGenerator.generateSubscription(user.id!!, 1L)
            dataGenerator.generateSubscription(user.id, 2L)

            // When & Then
            mvc
                .perform(
                    get("/api/v1/subscriptions")
                        .cookie(Cookie("AUTH-TOKEN", token)),
                ).andExpect(status().isOk)
                .andExpect(jsonPath("$.subscriptions").isArray)
                .andExpect(jsonPath("$.subscriptions", hasSize<Any>(2)))
        }

        @Test
        fun `deleteSubscription returns 204 after successful removal`() {
            // Given
            val (user, token) = dataGenerator.generateUser()
            val (_, token2) = dataGenerator.generateUser()
            val subscription = dataGenerator.generateSubscription(user.id!!, 1L)

            // When: Delete the subscription with another user
            mvc
                .perform(
                    delete("/api/v1/subscriptions/${subscription.id!!}")
                        .cookie(Cookie("AUTH-TOKEN", token2)),
                ).andExpect(status().isNotFound)

            // When: Delete the subscription
            mvc
                .perform(
                    delete("/api/v1/subscriptions/${subscription.id}")
                        .cookie(Cookie("AUTH-TOKEN", token)),
                ).andExpect(status().isNoContent)

            // Then: Verify it's gone
            mvc
                .perform(
                    get("/api/v1/subscriptions")
                        .cookie(Cookie("AUTH-TOKEN", token)),
                ).andExpect(status().isOk)
                .andExpect(jsonPath("$.subscriptions", hasSize<Any>(0)))
        }
    }

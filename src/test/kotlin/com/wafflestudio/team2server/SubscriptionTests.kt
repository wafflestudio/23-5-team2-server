package com.wafflestudio.team2server

import com.fasterxml.jackson.databind.ObjectMapper
import com.wafflestudio.team2server.helper.DataGenerator
import com.wafflestudio.team2server.subscription.dto.CreateSubscriptionRequest
import com.wafflestudio.team2server.subscription.dto.DeleteSubscriptionRequest
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
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
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
                .post("/api/v1/subscriptions") {
                    cookie(Cookie("AUTH-TOKEN", token))
                    contentType = MediaType.APPLICATION_JSON
                    content = mapper.writeValueAsString(request)
                }.andExpect {
                    status { isCreated() }
                    jsonPath("$.boardId").value(1L)
                }
        }

        @Test
        fun `getSubscriptions returns user's subscription list`() {
            // Given
            val (user, token) = dataGenerator.generateUser()

            // Directly creating subscriptions via dataGenerator
            // (Assuming you have a way to save these to the DB for the test user)
            dataGenerator.generateSubscription(user.id!!, 1L)
            dataGenerator.generateSubscription(user.id, 2L)

            // When & Then
            mvc
                .get("/api/v1/subscriptions") {
                    cookie(Cookie("AUTH-TOKEN", token))
                }.andExpect {
                    status { isOk() }
                    jsonPath("$.subscriptions").isArray()
                    jsonPath("$.subscriptions.length()").value(2)
                }
        }

        @Test
        fun `deleteSubscription returns 204 after successful removal`() {
            // Given
            val (user, token) = dataGenerator.generateUser()
            val (_, token2) = dataGenerator.generateUser()
            val subscription = dataGenerator.generateSubscription(user.id!!, 1L)

            // Assuming subscription.id is what the delete request requires
            val deleteRequest = DeleteSubscriptionRequest(subscriptionId = subscription.id!!)

            // When: Delete the subscription with another user
            mvc
                .delete("/api/v1/subscriptions") {
                    cookie(Cookie("AUTH-TOKEN", token2))
                    contentType = MediaType.APPLICATION_JSON
                    content = mapper.writeValueAsString(deleteRequest)
                }.andExpect {
                    status { isNotFound() }
                }

            // When: Delete the subscription
            mvc
                .delete("/api/v1/subscriptions") {
                    cookie(Cookie("AUTH-TOKEN", token))
                    contentType = MediaType.APPLICATION_JSON
                    content = mapper.writeValueAsString(deleteRequest)
                }.andExpect {
                    status { isNoContent() }
                }

            // Then: Verify it's gone
            mvc
                .get("/api/v1/subscriptions") {
                    cookie(Cookie("AUTH-TOKEN", token))
                }.andExpect {
                    status { isOk() }
                    jsonPath("$.subscriptions.length()").value(0)
                }
        }
    }

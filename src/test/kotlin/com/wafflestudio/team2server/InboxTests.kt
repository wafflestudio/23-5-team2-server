package com.wafflestudio.team2server

import com.fasterxml.jackson.databind.ObjectMapper
import com.wafflestudio.team2server.article.dto.request.CreateArticleRequest
import com.wafflestudio.team2server.helper.DataGenerator
import com.wafflestudio.team2server.inboxes.dto.InboxPagingResponse
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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.testcontainers.junit.jupiter.Testcontainers
import java.time.Instant

@SpringBootTest
@ActiveProfiles("test")
@Testcontainers
@AutoConfigureMockMvc
class InboxTests
    @Autowired
    constructor(
        private val mvc: MockMvc,
        private val mapper: ObjectMapper,
        private val dataGenerator: DataGenerator,
    ) {
        @Test
        fun `getInboxes returns paginated results and respects limit and nextId`() {
            // 1. Given: Create 3 inbox items
            val (user, token) = dataGenerator.generateUser()
            val inbox1 = dataGenerator.generateInbox(user.id!!, "Message 1") // Oldest
            val inbox2 = dataGenerator.generateInbox(user.id, "Message 2")
            val inbox3 = dataGenerator.generateInbox(user.id, "Message 3") // Newest

            // 2. When: Request first page with limit 2
            val res =
                mvc
                    .get("/api/v1/inboxes?limit=2") {
                        cookie(Cookie("AUTH-TOKEN", token))
                    }.andExpect {
                        status { isOk() }
                        jsonPath("$.inboxes.length()").value(2)
                        jsonPath("$.inboxes[0].id").value(inbox3.id)
                        jsonPath("$.inboxes[1].id").value(inbox2.id)
                        jsonPath("$.paging.hasNext").value(true)
                    }.andReturn()
                    .response
                    .getContentAsString(Charsets.UTF_8)
                    .let {
                        mapper.readValue(it, InboxPagingResponse::class.java)
                    }

            // 3. When: Request second page using the ID of the last item from page 1 as nextId
            mvc
                .get("/api/v1/inboxes?nextId=${res.paging.nextId}&limit=2") {
                    cookie(Cookie("AUTH-TOKEN", token))
                }.andExpect {
                    status { isOk() }
                    jsonPath("$.inboxes.length()").value(1)
                    jsonPath("$.inboxes[0].id").value(inbox1.id)
                    jsonPath("$.paging.hasNext").value(false)
                }
        }

        @Test
        fun `markInboxAsRead returns 204 after successful patch`() {
            // Given
            val (user, token) = dataGenerator.generateUser()
            val inbox = dataGenerator.generateInbox(user.id!!, "Unread Message")
            val inboxId = inbox.id!!

            // When
            mvc
                .patch("/api/v1/inboxes/$inboxId") {
                    cookie(Cookie("AUTH-TOKEN", token))
                }.andExpect {
                    status { isNoContent() }
                }

            // Then: Verify it is marked as read via a list check if your DTO includes isRead
            mvc
                .get("/api/v1/inboxes") {
                    cookie(Cookie("AUTH-TOKEN", token))
                }.andExpect {
                    status { isOk() }
                    jsonPath("$.inboxes[0].isRead").value(true)
                }
        }

        @Test
        fun `deleteInbox removes inbox record and returns 204`() {
            // Given
            val (user, token) = dataGenerator.generateUser()
            val inbox = dataGenerator.generateInbox(user.id!!, "Message to Delete")
            val inboxId = inbox.id!!

            // When: Delete the specific inbox record
            mvc
                .delete("/api/v1/inboxes/$inboxId") {
                    cookie(Cookie("AUTH-TOKEN", token))
                }.andExpect {
                    status { isNoContent() }
                }

            // Then: Verify the list is empty
            mvc
                .get("/api/v1/inboxes") {
                    cookie(Cookie("AUTH-TOKEN", token))
                }.andExpect {
                    status { isOk() }
                    jsonPath("$.inboxes.length()").value(0)
                }
        }

        @Test
        fun `markInboxAsRead returns 404 for inbox not belonging to user`() {
            // Given
            val (_, token1) = dataGenerator.generateUser()
            val (user2, _) = dataGenerator.generateUser()
            val user2Inbox = dataGenerator.generateInbox(user2.id!!, "User 2's Secret")

            // When: User 1 tries to mark User 2's inbox as read
            mvc
                .patch("/api/v1/inboxes/${user2Inbox.id}") {
                    cookie(Cookie("AUTH-TOKEN", token1))
                }.andExpect {
                    status { isNotFound() }
                }
        }

        @Test
        fun `creating an article should automatically create inboxes for all subscribers`() {
            // 1. Given: Three users, two subscribe to Board 1, one subscribes to Board 2

            val (user1, token1) = dataGenerator.generateUser()
            val (user2, token2) = dataGenerator.generateUser()
            val (user3, token3) = dataGenerator.generateUser()

            dataGenerator.generateSubscription(user1.id!!, 1)
            dataGenerator.generateSubscription(user2.id!!, 1)
            dataGenerator.generateSubscription(user3.id!!, 2)

            // 2. When: A new article is saved to Board 1
            val request =
                CreateArticleRequest(
                    title = "title",
                    content = "content",
                    author = "snu",
                    originLink = "https://example.com/article/123",
                    publishedAt = Instant.now(),
                )

            // when & then
            mvc
                .perform(
                    MockMvcRequestBuilders
                        .post("/api/v1/boards/1/articles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request)),
                ).andExpect(status().isOk)

            // 3. Then: User 1 and User 2 should have 1 inbox item, User 3 should have 0

            // Check User 1
            mvc
                .get("/api/v1/inboxes") {
                    cookie(Cookie("AUTH-TOKEN", token1))
                }.andExpect {
                    status { isOk() }
                    jsonPath("$.inboxes.length()").value(1)
                    jsonPath("$.inboxes[0].article.title").value("Breaking News")
                }

            // Check User 2
            mvc
                .get("/api/v1/inboxes") {
                    cookie(Cookie("AUTH-TOKEN", token2))
                }.andExpect {
                    status { isOk() }
                    jsonPath("$.inboxes.length()").value(1)
                }

            // Check User 3 (Subscribed to different board)
            mvc
                .get("/api/v1/inboxes") {
                    cookie(Cookie("AUTH-TOKEN", token3))
                }.andExpect {
                    status { isOk() }
                    jsonPath("$.inboxes.length()").value(0)
                }
        }
    }

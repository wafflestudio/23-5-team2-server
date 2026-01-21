package com.wafflestudio.team2server

import com.fasterxml.jackson.databind.ObjectMapper
import com.wafflestudio.team2server.dislike.dto.CreateDislikeRequest
import com.wafflestudio.team2server.helper.DataGenerator
import jakarta.servlet.http.Cookie
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
import java.util.concurrent.Executors

@SpringBootTest
@ActiveProfiles("test")
@Testcontainers
@AutoConfigureMockMvc
class DislikeTests
    @Autowired
    constructor(
        private val mvc: MockMvc,
        private val mapper: ObjectMapper,
        private val dataGenerator: DataGenerator,
    ) {
        @Test
        fun `dislike lifecycle updates article dislike dislikes`() {
            // 1. Given: A user and an article
            val (_, token) = dataGenerator.generateUser()
            val article = dataGenerator.generateArticle()
            val articleId = article.id!!
            val createRequest = CreateDislikeRequest(articleId = articleId)

            // 2. When: Create a dislike
            mvc
                .perform(
                    post("/api/v1/dislikes")
                        .cookie(Cookie("AUTH-TOKEN", token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(createRequest)),
                ).andExpect(status().isCreated)
                .andExpect(jsonPath("$.dislikes").value(1))

            // 3. Then: Check if dislike record exists (204 No Content)
            mvc
                .perform(
                    get("/api/v1/dislikes/$articleId")
                        .cookie(Cookie("AUTH-TOKEN", token)),
                ).andExpect(status().isNoContent)

            // 4. Then: Verify ArticleDto reflects the dislikes via ArticleController
            mvc
                .perform(
                    get("/api/v1/articles/$articleId"),
                ).andExpect(status().isOk)
                .andExpect(jsonPath("$.dislikes").value(1))

            // 5. When: Delete the dislike
            mvc
                .perform(
                    delete("/api/v1/dislikes/$articleId")
                        .cookie(Cookie("AUTH-TOKEN", token)),
                ).andExpect(status().isOk)
                .andExpect(jsonPath("$.dislikes").value(0))

            // 6. Then: Verify dislikes is back to 0 in the article
            mvc
                .perform(
                    get("/api/v1/articles/$articleId"),
                ).andExpect(status().isOk)
                .andExpect(jsonPath("$.dislikes").value(0))
        }

        @Test
        fun `getDislike returns 404 when no dislike record exists`() {
            // Given
            val (_, token) = dataGenerator.generateUser()
            val articleId = 999L // Non-existent or not disliked

            // When & Then
            mvc
                .perform(
                    get("/api/v1/dislikes/$articleId")
                        .cookie(Cookie("AUTH-TOKEN", token)),
                ).andExpect(status().isNotFound)
        }

        @Test
        fun `multiple users disliking same article increases dislikes`() {
            // Given
            val (_, token1) = dataGenerator.generateUser()
            val (_, token2) = dataGenerator.generateUser()
            val article = dataGenerator.generateArticle()
            val articleId = article.id!!
            val request = CreateDislikeRequest(articleId = articleId)

            // When: User 1 dislikes
            mvc
                .perform(
                    post("/api/v1/dislikes")
                        .cookie(Cookie("AUTH-TOKEN", token1))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request)),
                ).andExpect(status().isCreated)
                .andExpect(jsonPath("$.dislikes").value(1))

            // When: User 2 dislikes
            mvc
                .perform(
                    post("/api/v1/dislikes")
                        .cookie(Cookie("AUTH-TOKEN", token2))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request)),
                ).andExpect(status().isCreated)
                .andExpect(jsonPath("$.dislikes").value(2))

            // Then: Fetch article and verify aggregate dislikes
            mvc
                .perform(
                    get("/api/v1/articles/$articleId"),
                ).andExpect(status().isOk)
                .andExpect(jsonPath("$.dislikes").value(2))
        }

        @Test
        fun `should increment dislike dislikes by 1 even when multiple concurrent dislike requests are made`() {
            // Given: A user and an article
            val threadPool = Executors.newFixedThreadPool(4)
            val article = dataGenerator.generateArticle()
            val (_, token) = dataGenerator.generateUser()
            val request = CreateDislikeRequest(articleId = article.id!!)

            // When: 4 concurrent threads attempt to dislike the same article for the same user
            val jobs =
                List(4) {
                    threadPool.submit {
                        mvc
                            .perform(
                                post("/api/v1/dislikes")
                                    .cookie(Cookie("AUTH-TOKEN", token))
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(mapper.writeValueAsString(request)),
                            ).andExpect(status().isCreated)
                    }
                }
            jobs.forEach { it.get() }

            // Then: Total dislike dislikes should only be 1
            mvc
                .perform(
                    get("/api/v1/articles/${article.id}"),
                ).andExpect(status().isOk)
                .andExpect(jsonPath("$.dislikes").value(1))
        }

        @Test
        fun `should decrement dislike dislikes by 1 even when multiple concurrent delete requests are made`() {
            // Given: User has already disliked the article
            val threadPool = Executors.newFixedThreadPool(4)
            val article = dataGenerator.generateArticle()
            val (_, token) = dataGenerator.generateUser()

            // Initial dislike
            val request = CreateDislikeRequest(articleId = article.id!!)
            mvc
                .perform(
                    post("/api/v1/dislikes")
                        .cookie(Cookie("AUTH-TOKEN", token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request)),
                ).andExpect(status().isCreated)

            // When: 4 concurrent threads attempt to delete the same dislike
            val jobs =
                List(4) {
                    threadPool.submit {
                        mvc
                            .perform(
                                delete("/api/v1/dislikes/${article.id}")
                                    .cookie(Cookie("AUTH-TOKEN", token)),
                            ).andExpect(status().isOk)
                    }
                }
            jobs.forEach { it.get() }

            // Then: Total dislike dislikes should be 0
            mvc
                .perform(
                    get("/api/v1/articles/${article.id}"),
                ).andExpect(status().isOk)
                .andExpect(jsonPath("$.dislikes").value(0))
        }

        @Test
        fun `should not change dislike dislikes when deleting a dislike that was not made by the user`() {
            // Given: Another user disliked the article, but current user did not
            val article = dataGenerator.generateArticle()
            val (_, token1) = dataGenerator.generateUser()

            val request = CreateDislikeRequest(articleId = article.id!!)
            mvc
                .perform(
                    post("/api/v1/dislikes")
                        .cookie(Cookie("AUTH-TOKEN", token1))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request)),
                ).andExpect(status().isCreated)

            val (_, token2) = dataGenerator.generateUser()

            // When: Current user (who hasn't disliked) tries to delete a dislike for this article
            mvc
                .perform(
                    delete("/api/v1/dislikes/${article.id}")
                        .cookie(Cookie("AUTH-TOKEN", token2)),
                ).andExpect(status().isOk)
                .andExpect(jsonPath("$.dislikes").value(1))

            // Then: Total dislike dislikes remains 1
            mvc
                .perform(
                    get("/api/v1/articles/${article.id}"),
                ).andExpect(status().isOk)
                .andExpect(jsonPath("$.dislikes").value(1))
        }
    }

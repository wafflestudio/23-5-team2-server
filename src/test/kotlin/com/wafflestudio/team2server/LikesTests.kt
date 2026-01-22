package com.wafflestudio.team2server

import com.fasterxml.jackson.databind.ObjectMapper
import com.wafflestudio.team2server.helper.DataGenerator
import com.wafflestudio.team2server.like.dto.CreateLikeRequest
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
class LikesTests
    @Autowired
    constructor(
        private val mvc: MockMvc,
        private val mapper: ObjectMapper,
        private val dataGenerator: DataGenerator,
    ) {
        @Test
        fun `like lifecycle updates article like likes`() {
            val (_, token) = dataGenerator.generateUser()
            val article = dataGenerator.generateArticle()
            val articleId = article.id!!
            val createRequest = CreateLikeRequest(articleId = articleId)

            mvc
                .perform(
                    post("/api/v1/likes")
                        .cookie(Cookie("AUTH-TOKEN", token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(createRequest)),
                ).andExpect(status().isCreated)
                .andExpect(jsonPath("$.likes").value(1))

            mvc
                .perform(
                    get("/api/v1/likes/$articleId")
                        .cookie(Cookie("AUTH-TOKEN", token)),
                ).andExpect(status().isNoContent)

            mvc
                .perform(
                    get("/api/v1/articles/$articleId"),
                ).andExpect(status().isOk)
                .andExpect(jsonPath("$.likes").value(1))

            mvc
                .perform(
                    delete("/api/v1/likes/$articleId")
                        .cookie(Cookie("AUTH-TOKEN", token)),
                ).andExpect(status().isOk)
                .andExpect(jsonPath("$.likes").value(0))

            mvc
                .perform(
                    get("/api/v1/articles/$articleId"),
                ).andExpect(status().isOk)
                .andExpect(jsonPath("$.likes").value(0))
        }

        @Test
        fun `getLike returns 404 when no like record exists`() {
            val (_, token) = dataGenerator.generateUser()
            val articleId = 999L

            mvc
                .perform(
                    get("/api/v1/likes/$articleId")
                        .cookie(Cookie("AUTH-TOKEN", token)),
                ).andExpect(status().isNotFound)
        }

        @Test
        fun `multiple users liking same article increases likes`() {
            val (_, token1) = dataGenerator.generateUser()
            val (_, token2) = dataGenerator.generateUser()
            val article = dataGenerator.generateArticle()
            val articleId = article.id!!
            val request = CreateLikeRequest(articleId = articleId)

            mvc
                .perform(
                    post("/api/v1/likes")
                        .cookie(Cookie("AUTH-TOKEN", token1))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request)),
                ).andExpect(status().isCreated)
                .andExpect(jsonPath("$.likes").value(1))

            mvc
                .perform(
                    post("/api/v1/likes")
                        .cookie(Cookie("AUTH-TOKEN", token2))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request)),
                ).andExpect(status().isCreated)
                .andExpect(jsonPath("$.likes").value(2))

            mvc
                .perform(
                    get("/api/v1/articles/$articleId"),
                ).andExpect(status().isOk)
                .andExpect(jsonPath("$.likes").value(2))
        }

        @Test
        fun `should increment like likes by 1 even when multiple concurrent like requests are made`() {
            val threadPool = Executors.newFixedThreadPool(4)
            val article = dataGenerator.generateArticle()
            val (_, token) = dataGenerator.generateUser()
            val request = CreateLikeRequest(articleId = article.id!!)

            val jobs =
                List(4) {
                    threadPool.submit {
                        mvc
                            .perform(
                                post("/api/v1/likes")
                                    .cookie(Cookie("AUTH-TOKEN", token))
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(mapper.writeValueAsString(request)),
                            ).andExpect(status().isCreated)
                    }
                }
            jobs.forEach { it.get() }

            mvc
                .perform(
                    get("/api/v1/articles/${article.id}"),
                ).andExpect(status().isOk)
                .andExpect(jsonPath("$.likes").value(1))
        }

        @Test
        fun `should decrement like likes by 1 even when multiple concurrent delete requests are made`() {
            val threadPool = Executors.newFixedThreadPool(4)
            val article = dataGenerator.generateArticle()
            val (_, token) = dataGenerator.generateUser()

            val request = CreateLikeRequest(articleId = article.id!!)
            mvc
                .perform(
                    post("/api/v1/likes")
                        .cookie(Cookie("AUTH-TOKEN", token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request)),
                ).andExpect(status().isCreated)

            val jobs =
                List(4) {
                    threadPool.submit {
                        mvc
                            .perform(
                                delete("/api/v1/likes/${article.id}")
                                    .cookie(Cookie("AUTH-TOKEN", token)),
                            ).andExpect(status().isOk)
                    }
                }
            jobs.forEach { it.get() }

            mvc
                .perform(
                    get("/api/v1/articles/${article.id}"),
                ).andExpect(status().isOk)
                .andExpect(jsonPath("$.likes").value(0))
        }

        @Test
        fun `should not change like likes when deleting a like that was not made by the user`() {
            val article = dataGenerator.generateArticle()
            val (_, token1) = dataGenerator.generateUser()

            val request = CreateLikeRequest(articleId = article.id!!)
            mvc
                .perform(
                    post("/api/v1/likes")
                        .cookie(Cookie("AUTH-TOKEN", token1))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request)),
                ).andExpect(status().isCreated)

            val (_, token2) = dataGenerator.generateUser()

            mvc
                .perform(
                    delete("/api/v1/likes/${article.id}")
                        .cookie(Cookie("AUTH-TOKEN", token2)),
                ).andExpect(status().isOk)
                .andExpect(jsonPath("$.likes").value(1))

            mvc
                .perform(
                    get("/api/v1/articles/${article.id}"),
                ).andExpect(status().isOk)
                .andExpect(jsonPath("$.likes").value(1))
        }
    }

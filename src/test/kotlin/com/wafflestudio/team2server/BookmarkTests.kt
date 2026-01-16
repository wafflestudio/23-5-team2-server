package com.wafflestudio.team2server

import com.fasterxml.jackson.databind.ObjectMapper
import com.wafflestudio.team2server.bookmark.dto.BookmarkPagingResponse
import com.wafflestudio.team2server.bookmark.dto.CreateBookmarkRequest
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
class BookmarkTests
    @Autowired
    constructor(
        private val mvc: MockMvc,
        private val mapper: ObjectMapper,
        private val dataGenerator: DataGenerator,
    ) {
        @Test
        fun `createBookmark returns 201 and count`() {
            // Given
            val (_, token) = dataGenerator.generateUser()
            val article = dataGenerator.generateArticle()
            val request = CreateBookmarkRequest(articleId = article.id!!)

            // When & Then
            mvc
                .perform(
                    post("/api/v1/bookmarks")
                        .cookie(Cookie("AUTH-TOKEN", token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request)),
                ).andExpect(status().isCreated)
                .andExpect(jsonPath("$.count").value(1))

            mvc
                .perform(
                    post("/api/v1/bookmarks")
                        .cookie(Cookie("AUTH-TOKEN", token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request)),
                ).andExpect(status().isCreated)
                .andExpect(jsonPath("$.count").value(0))
        }

        @Test
        fun `getBookmarks returns paginated list of bookmarks`() {
            // Given
            val (user, token) = dataGenerator.generateUser()
            val article1 = dataGenerator.generateArticle()
            val article2 = dataGenerator.generateArticle()

            dataGenerator.generateBookmark(user.id!!, article1.id!!)
            dataGenerator.generateBookmark(user.id, article2.id!!)

            // When: First page with limit 1
            val mvcResult =
                mvc
                    .perform(
                        get("/api/v1/bookmarks?limit=1")
                            .cookie(Cookie("AUTH-TOKEN", token)),
                    ).andExpect(status().isOk)
                    .andExpect(jsonPath("$.bookmarks", hasSize<Any>(1)))
                    .andExpect(jsonPath("$.paging.hasNext").value(true))
                    .andReturn()

            val response =
                mapper.readValue(
                    mvcResult.response.getContentAsString(Charsets.UTF_8),
                    BookmarkPagingResponse::class.java,
                )

            // Then: Second page using nextId
            mvc
                .perform(
                    get("/api/v1/bookmarks?nextId=${response.paging.nextId}&limit=1")
                        .cookie(Cookie("AUTH-TOKEN", token)),
                ).andExpect(status().isOk)
                .andExpect(jsonPath("$.bookmarks", hasSize<Any>(1)))
                .andExpect(jsonPath("$.paging.hasNext").value(false))
        }

        @Test
        fun `deleteBookmark returns 200 and removes entry`() {
            // Given
            val (user, token) = dataGenerator.generateUser()
            val (_, token2) = dataGenerator.generateUser()
            val article = dataGenerator.generateArticle()
            val bookmark = dataGenerator.generateBookmark(user.id!!, article.id!!)

            // When: Delete
            mvc
                .perform(
                    delete("/api/v1/bookmarks/${bookmark.id}")
                        .cookie(Cookie("AUTH-TOKEN", token2)),
                ).andExpect(status().isOk)
                .andExpect(jsonPath("$.count").value(0))

            mvc
                .perform(
                    delete("/api/v1/bookmarks/${bookmark.id}")
                        .cookie(Cookie("AUTH-TOKEN", token)),
                ).andExpect(status().isOk)
                .andExpect(jsonPath("$.count").value(1))

            // Then: Verify empty list
            mvc
                .perform(
                    get("/api/v1/bookmarks")
                        .cookie(Cookie("AUTH-TOKEN", token)),
                ).andExpect(status().isOk)
                .andExpect(jsonPath("$.bookmarks", hasSize<Any>(0)))
        }
    }

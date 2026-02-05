package com.wafflestudio.team2server

import com.fasterxml.jackson.databind.ObjectMapper
import com.wafflestudio.team2server.article.dto.UpdateArticleRequest
import com.wafflestudio.team2server.article.dto.core.ArticleDto
import com.wafflestudio.team2server.article.dto.request.CreateArticleRequest
import com.wafflestudio.team2server.article.dto.response.ArticlePagingResponse
import com.wafflestudio.team2server.article.model.Article
import com.wafflestudio.team2server.article.repository.ArticleRepository
import com.wafflestudio.team2server.article.service.ArticleService
import com.wafflestudio.team2server.helper.DataGenerator
import com.wafflestudio.team2server.helper.QueryCounter
import jakarta.servlet.http.Cookie
import org.hamcrest.Matchers.containsString
import org.hamcrest.Matchers.hasSize
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.testcontainers.junit.jupiter.Testcontainers
import java.time.Instant
import java.util.concurrent.Executors

@SpringBootTest
@ActiveProfiles("test")
@Testcontainers
@AutoConfigureMockMvc
class ArticleIntegrationTests
    @Autowired
    constructor(
        private val dataGenerator: DataGenerator,
        private val queryCounter: QueryCounter,
        private val mvc: MockMvc,
        private val mapper: ObjectMapper,
        private val articleService: ArticleService,
        private val articleRepository: ArticleRepository,
    ) {
        @BeforeEach
        fun cleanDummyBoardArticles() {
            articleRepository.deleteAllByBoardId(1L)
        }

        @Test
        fun `should create a article`() {
            val request =
                CreateArticleRequest(
                    title = "title",
                    content = "content",
                    author = "snu",
                    originLink = null,
                    publishedAt = Instant.now(),
                )

            mvc
                .perform(
                    post("/api/v1/articles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request)),
                ).andExpect(status().isCreated)
                .andExpect(jsonPath("$.title").value(request.title))
                .andExpect(jsonPath("$.content").value(request.content))
                .andExpect(jsonPath("$.author").value(request.author))
                .andExpect(jsonPath("$.originLink").value(request.originLink))
        }

        @Disabled
        @Test
        fun `should not create a with blank title`() {
            val request =
                CreateArticleRequest(
                    title = "   ",
                    content = "content",
                    author = "snu",
                    originLink = null,
                    publishedAt = Instant.now(),
                )

            mvc
                .perform(
                    post("/api/v1/articles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request)),
                ).andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.title").value(request.title))
                .andExpect(jsonPath("$.content").value(request.content))
                .andExpect(jsonPath("$.author").value(request.author))
                .andExpect(jsonPath("$.originLink").value(request.originLink))
        }

        @Test
        fun `should retrieve a single article`() {
            val article = dataGenerator.generateArticle()

            mvc
                .perform(get("/api/v1/articles/${article.id}"))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.id").value(article.id))
                .andExpect(jsonPath("$.title").value(article.title))
                .andExpect(jsonPath("$.content").value(article.content))
                .andExpect(jsonPath("$.author").value(article.author))
                .andExpect(jsonPath("$.originLink").value(article.originLink))
        }

        @Test
        fun `should update a article`() {
            val article = dataGenerator.generateArticle()
            val request =
                UpdateArticleRequest(
                    title = "sesese",
                    content = "uugg",
                )

            mvc
                .perform(
                    patch("/api/v1/articles/${article.id}")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request)),
                ).andExpect(status().isOk)
                .andExpect(jsonPath("$.id").value(article.id))
                .andExpect(jsonPath("$.title").value(request.title))
                .andExpect(jsonPath("$.content").value(request.content))
                .andExpect(jsonPath("$.author").value(article.author))
        }

        @Disabled
        @Test
        fun `should not update a article with blank title or content`() {
            val article = dataGenerator.generateArticle()
            val request =
                UpdateArticleRequest(
                    title = "sesese",
                    content = " ",
                )

            mvc
                .perform(
                    patch("/api/v1/articles/${article.id}")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request)),
                ).andExpect(status().isBadRequest)
        }

        @Test
        fun `should delete a article`() {
            val article = dataGenerator.generateArticle()

            mvc
                .perform(delete("/api/v1/articles/${article.id}"))
                .andExpect(status().isNoContent)

            mvc
                .perform(delete("/api/v1/articles/${article.id}"))
                .andExpect(status().isNotFound)
        }

        @Test
        fun `should paginate posts using published_at and id as cursor`() {
            repeat(30) {
                dataGenerator.generateArticle()
            }

            val mvcResult =
                mvc
                    .perform(
                        get("/api/v1/articles")
                            .param("limit", "15")
                            .param("boardIds", "1,"),
                    ).andExpect(status().isOk)
                    .andExpect(jsonPath("$.paging.hasNext").value(true))
                    .andReturn()

            val response =
                mapper.readValue(
                    mvcResult.response.getContentAsString(Charsets.UTF_8),
                    ArticlePagingResponse::class.java,
                )

            assertArticlesAreSorted(response.data)
            assertArticlesAreInBoard(response.data)

            val nextMvcResult =
                mvc
                    .perform(
                        get("/api/v1/articles")
                            .param("boardIds", "1,")
                            .param("limit", "15")
                            .param("nextPublishedAt", response.paging.nextPublishedAt!!.toString())
                            .param("nextId", response.paging.nextId!!.toString())
                            .accept(MediaType.APPLICATION_JSON),
                    ).andExpect(status().isOk)
                    .andExpect(jsonPath("$.paging.hasNext").value(false))
                    .andReturn()

            val nextResponse =
                mapper.readValue(
                    nextMvcResult.response.getContentAsString(Charsets.UTF_8),
                    ArticlePagingResponse::class.java,
                )

            assertArticlesAreSorted(nextResponse.data)
            assertArticlesAreInBoard(nextResponse.data)
            assertTrue((response.data.map { it.id } + nextResponse.data.map { it.id }).toSet().size == 30)
        }

        @Test
        fun `Only two queries are fired during pagination`() {
            val articles = List(10) { dataGenerator.generateArticle() }

            val response =
                queryCounter.assertQueryCount(1) {
                    val mvcResult =
                        mvc
                            .perform(
                                get("/api/v1/articles")
                                    .param("limit", "20")
                                    .param("boardIds", "1,"),
                            ).andExpect(status().isOk)
                            .andReturn()

                    mapper.readValue(
                        mvcResult.response.getContentAsString(Charsets.UTF_8),
                        ArticlePagingResponse::class.java,
                    )
                }
            assertArticlesAreSame(response.data, articles)
        }

        @Test
        fun `should filter articles by keyword`() {
            dataGenerator.generateArticle(title = "waffle")
            dataGenerator.generateArticle(title = "eus")

            mvc
                .perform(
                    get("/api/v1/articles")
                        .param("limit", "15")
                        .param("boardIds", "1,")
                        .param("keyword", "waffle")
                        .accept(MediaType.APPLICATION_JSON),
                ).andExpect(status().isOk)
                .andExpect(jsonPath("$.data", hasSize<Any>(1)))
                .andExpect(jsonPath("$.data[0].title").value(containsString("waffle")))
        }

        @Test
        fun `should increase views by number of detail get requests`() {
            val threadPool = Executors.newFixedThreadPool(4)
            val article = dataGenerator.generateArticle()

            val jobs =
                List(4) {
                    threadPool.submit {
                        mvc
                            .perform(
                                get("/api/v1/articles/{articleId}", article.id)
                                    .contentType(MediaType.APPLICATION_JSON),
                            ).andExpect(status().isOk)
                    }
                }
            jobs.forEach { it.get() }

            mvc
                .perform(
                    get("/api/v1/articles")
                        .contentType(MediaType.APPLICATION_JSON),
                ).andExpect(status().isOk)
                .andExpect(
                    jsonPath("$.data[?(@.id == ${article.id})].views").value(4),
                )
        }

        @Test
        fun `should promote article to hot board based on view count`() {
            val article = dataGenerator.generateArticle()

            mvc
                .perform(
                    get("/api/v1/articles/hots")
                        .param("limit", "20")
                        .contentType(MediaType.APPLICATION_JSON),
                ).andExpect(status().isOk)
                .andExpect(jsonPath("$.data[?(@.id == ${article.id})]").isEmpty)

            repeat(5) {
                mvc
                    .perform(
                        get("/api/v1/articles/{articleId}", article.id)
                            .contentType(MediaType.APPLICATION_JSON),
                    ).andExpect(status().isOk)
            }

            mvc
                .perform(
                    patch("/api/v1/articles/hots")
                        .cookie(Cookie("AUTH-TOKEN", dataGenerator.generateToken("admin")))
                        .param("hotScore", "4")
                        .contentType(MediaType.APPLICATION_JSON),
                ).andExpect(status().isOk)
                .andExpect(jsonPath("$.hotScore").value(4))

            mvc
                .perform(
                    get("/api/v1/articles/hots")
                        .param("limit", "20")
                        .contentType(MediaType.APPLICATION_JSON),
                ).andExpect(status().isOk)
                .andExpect(jsonPath("$.data[?(@.id == ${article.id})]").isNotEmpty)
        }

        private fun assertArticlesAreSorted(articles: List<ArticleDto>) {
            if (articles.size <= 1) return
            articles.zipWithNext().forEach { (current, next) ->
                assertTrue(current.publishedAt >= next.publishedAt)
                if (current.publishedAt == next.publishedAt) {
                    assertTrue(current.id > next.id)
                }
            }
        }

        private fun assertArticlesAreInBoard(articles: List<ArticleDto>) {
            articles.forEach { assertTrue(it.board.id == 1L) }
        }

        private fun assertArticlesAreSame(
            targetArticles: List<ArticleDto>,
            originalArticles: List<Article>,
        ) {
            val originalArticleDtos =
                originalArticles
                    .map { articleService.get(articleId = it.id!!) }
                    .sortedWith(compareByDescending<ArticleDto> { it.publishedAt }.thenByDescending { it.id })
            assertTrue(targetArticles.size == originalArticleDtos.size)
            targetArticles.zip(originalArticleDtos).forEach { (target, expected) ->
                assertTrue(target.id == expected.id)
            }
        }
    }

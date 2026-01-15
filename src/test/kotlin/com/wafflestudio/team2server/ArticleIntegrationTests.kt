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
import org.springframework.test.web.servlet.delete
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.patch
import org.springframework.test.web.servlet.post
import org.testcontainers.junit.jupiter.Testcontainers
import java.time.Instant

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
            // given
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
                .post("/api/v1/boards/1/articles") {
                    contentType = MediaType.APPLICATION_JSON
                    content = mapper.writeValueAsString(request)
                }.andExpect {
                    status { isOk() }
                    jsonPath("$.title") { value(request.title) }
                    jsonPath("$.content") { value(request.content) }
                    jsonPath("$.author") { value(request.author) }
                    jsonPath("$.originLink") { value(request.originLink) }
                }
        }

        @Disabled
        @Test
        fun `should not create a with blank title`() {
            // title이 blank이면 article을 생성할 수 없다.
            // given
            val request =
                CreateArticleRequest(
                    title = "   ", // blank
                    content = "content",
                    author = "snu",
                    originLink = "https://example.com/article/123",
                    publishedAt = Instant.now(),
                )

            // when & then
            mvc
                .post("/api/v1/boards/1/articles") {
                    contentType = MediaType.APPLICATION_JSON
                    content = mapper.writeValueAsString(request)
                }.andExpect {
                    status { isBadRequest() }
                    jsonPath("$.title") { value(request.title) }
                    jsonPath("$.content") { value(request.content) }
                    jsonPath("$.author") { value(request.author) }
                    jsonPath("$.originLink") { value(request.originLink) }
                }
        }

        @Test
        fun `should retrieve a single article`() {
            // 게시글을 단건 조회 할 수 있다.
            // given
            val article = dataGenerator.generateArticle()

            // when & then
            mvc
                .get("/api/v1/articles/${article.id}")
                .andExpect {
                    status { isOk() }
                    jsonPath("$.id") { value(article.id) }
                    jsonPath("$.title") { value(article.title) }
                    jsonPath("$.content") { value(article.content) }
                    jsonPath("$.author") { value(article.author) }
                    jsonPath("$.originLink") { value(article.originLink) }
                }
        }

        @Test
        fun `should update a article`() {
            // 게시글 업데이트가 가능하다
            // given
            val article = dataGenerator.generateArticle()
            val request =
                UpdateArticleRequest(
                    title = "sesese",
                    content = "uugg",
                )
            // when & then
            mvc
                .patch("/api/v1/articles/${article.id}") {
                    contentType = MediaType.APPLICATION_JSON
                    content = mapper.writeValueAsString(request)
                }.andExpect {
                    status { isOk() }
                    jsonPath("$.id") { value(article.id) }
                    jsonPath("$.title") { value(request.title) }
                    jsonPath("$.content") { value(request.content) }
                    jsonPath("$.author") { value(article.author) }
                }
        }

        @Disabled
        @Test
        fun `should not update a article with blank title or content`() {
            // blank 값으로는 게시글 없데이트가 불가하다.
            // given
            val article = dataGenerator.generateArticle()
            val request =
                UpdateArticleRequest(
                    title = "sesese",
                    content = " ",
                )
            // when & then
            mvc
                .patch("/api/v1/articles/${article.id}") {
                    contentType = MediaType.APPLICATION_JSON
                    content = mapper.writeValueAsString(request)
                }.andExpect {
                    status { isBadRequest() }
                }
        }

        @Test
        fun `should delete a article`() {
            // 게시글 삭제가 가능하다
            // given
            val article = dataGenerator.generateArticle()
            // when & then
            mvc
                .delete("/api/v1/articles/${article.id}")
                .andExpect {
                    status { isNoContent() }
                }
            mvc
                .delete("/api/v1/articles/${article.id}")
                .andExpect {
                    status { isNotFound() }
                }
        }

        @Test
        fun `should paginate posts using published_at and id as cursor`() {
            // publised_at과 id를 커서로 하여 게시판의 게시글을 페이지네이션 함
            // given
            repeat(30) {
                dataGenerator.generateArticle()
            }
            // when & then
            val response =
                mvc
                    .get("/api/v1/articles") {
                        param("limit", "15")
                        param("boardIds", "1,")
                    }.andExpect {
                        status { isOk() }
                        jsonPath("$.paging.hasNext") { value(true) }
                    }.andReturn()
                    .response
                    .getContentAsString(Charsets.UTF_8)
                    .let { mapper.readValue(it, ArticlePagingResponse::class.java) }

            assertArticlesAreSorted(response.data)
            assertArticlesAreInBoard(response.data)

            val nextResponse =
                mvc
                    .get("/api/v1/articles") {
                        param("boardIds", "1,")
                        param("limit", "15")
                        param("nextPublishedAt", response.paging.nextPublishedAt!!.toString())
                        param("nextId", response.paging.nextId!!.toString())
                        accept = MediaType.APPLICATION_JSON
                    }.andExpect {
                        status { isOk() }
                        jsonPath("$.paging.hasNext") { value(false) }
                    }.andReturn()
                    .response
                    .getContentAsString(Charsets.UTF_8)
                    .let { mapper.readValue(it, ArticlePagingResponse::class.java) }

            assertArticlesAreSorted(nextResponse.data)
            assertArticlesAreInBoard(nextResponse.data)
            assertTrue((response.data.map { it.id } + nextResponse.data.map { it.id }).toSet().size == 30)
        }

        @Test
        fun `Only two queries are fired during pagination`() {
            // 쿼리는 두번만 나간다
            // given
            val articles =
                List(10) {
                    dataGenerator.generateArticle()
                }
            val response =
                queryCounter.assertQueryCount(1) {
                    mvc
                        .get("/api/v1/articles") {
                            param("limit", "20")
                            param("boardIds", "1,")
                        }.andExpect {
                            status { isOk() }
                        }.andReturn()
                        .response
                        .getContentAsString(Charsets.UTF_8)
                        .let {
                            mapper.readValue(it, ArticlePagingResponse::class.java)
                        }
                }
            assertArticlesAreSame(response.data, articles)
        }

        @Test
        fun `should filter articles by keyword`() {
            // 키워드 필터가 가능하다.
            // given

            dataGenerator.generateArticle(
                title = "waffle",
            )

            dataGenerator.generateArticle(
                title = "eus",
            )
            // when & then
            mvc
                .get("/api/v1/articles") {
                    param("limit", "15")
                    param("boardIds", "1,")
                    param("keyword", "waffle")
                    accept = MediaType.APPLICATION_JSON
                }.andExpect {
                    status { isOk() }
                    jsonPath("$.data.length()") { value(1) }
                    jsonPath("$.data[0].title") {
                        value(org.hamcrest.Matchers.containsString("waffle"))
                    }
                }
        }

        private fun assertArticlesAreSorted(articles: List<ArticleDto>) {
            if (articles.size <= 1) return

            articles.zipWithNext().forEach { (current, next) ->
                assertTrue(
                    current.publishedAt >= next.publishedAt,
                    "Articles are not sorted by publishedAt DESC. Failed at id ${current.id} -> ${next.id}",
                )

                if (current.publishedAt == next.publishedAt) {
                    assertTrue(
                        current.id > next.id,
                        "Articles with same publishedAt are not sorted by id DESC. Failed at id ${current.id} -> ${next.id}",
                    )
                }
            }
        }

        private fun assertArticlesAreInBoard(articles: List<ArticleDto>) {
            articles.forEach {
                assertTrue(it.board.id == 1L)
            }
        }

        private fun assertArticlesAreSame(
            targetArticles: List<ArticleDto>,
            originalArticles: List<Article>,
        ) {
            val originalArticleDtos =
                originalArticles
                    .map { articleService.get(articleId = it.id!!) }
                    .sortedWith(
                        compareByDescending<ArticleDto> { it.publishedAt }
                            .thenByDescending { it.id },
                    )
            assertTrue(targetArticles.size == originalArticleDtos.size)

            targetArticles.zip(originalArticleDtos).forEach { (target, expected) ->
                assertTrue(target.id == expected.id)
                assertTrue(target.board.id == expected.board.id)
            }
        }
    }

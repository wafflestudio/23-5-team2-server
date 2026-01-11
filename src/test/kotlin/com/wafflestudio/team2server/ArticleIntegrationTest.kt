package com.wafflestudio.team2server

import com.fasterxml.jackson.databind.ObjectMapper
import com.wafflestudio.team2server.article.dto.UpdateArticleRequest
import com.wafflestudio.team2server.article.dto.core.ArticleDto
import com.wafflestudio.team2server.article.dto.request.CreateArticleRequest
import com.wafflestudio.team2server.article.dto.response.ArticlePagingResponse
import com.wafflestudio.team2server.article.model.Article
import com.wafflestudio.team2server.article.service.ArticleService
import com.wafflestudio.team2server.board.model.Board
import com.wafflestudio.team2server.helper.DataGenerator
import com.wafflestudio.team2server.helper.QueryCounter
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.testcontainers.junit.jupiter.Testcontainers
import java.time.Instant

@SpringBootTest
@ActiveProfiles("test")
@Testcontainers
@AutoConfigureMockMvc
class ArticleIntegrationTest
    @Autowired
    constructor(
        private val dataGenerator: DataGenerator,
        private val queryCounter: QueryCounter,
        private val mvc: MockMvc,
        private val mapper: ObjectMapper,
        private val articleService: ArticleService,
    ) {
        @Test
        fun `should create a article`() {
            // given
            val board = dataGenerator.generateBoard()
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
                        .post("/api/v1/boards/${board.id!!}/articles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request)),
                ).andExpect(status().isOk)
                .andExpect(jsonPath("$.title").value(request.title))
                .andExpect(jsonPath("$.content").value(request.content))
                .andExpect(jsonPath("$.author").value(request.author))
                .andExpect(jsonPath("$.originLink").value(request.originLink))
        }

        @Test
        fun `should not create a with blank title`() {
            // title이 blank이면 article을 생성할 수 없다.
            // given
            val board = dataGenerator.generateBoard()
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
                .perform(
                    MockMvcRequestBuilders
                        .post("/api/v1/boards/${board.id!!}/articles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request)),
                ).andExpect(status().isBadRequest)
        }

        @Test
        fun `should retrieve a single article`() {
            // 게시글을 단건 조회 할 수 있다.
            // given
            val article = dataGenerator.generateArticle()

            // when & then
            mvc
                .perform(
                    MockMvcRequestBuilders.get("/api/v1/articles/${article.id}"),
                ).andExpect(status().isOk)
                .andExpect(jsonPath("$.id").value(article.id))
                .andExpect(jsonPath("$.title").value(article.title))
                .andExpect(jsonPath("$.content").value(article.content))
                .andExpect(jsonPath("$.author").value(article.author))
                .andExpect(jsonPath("$.originLink").value(article.originLink))
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
                .perform(
                    MockMvcRequestBuilders
                        .patch("/api/v1/articles/${article.id}")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request)),
                ).andExpect(status().isOk)
                .andExpect(jsonPath("$.id").value(article.id))
                .andExpect(jsonPath("$.title").value(request.title))
                .andExpect(jsonPath("$.content").value(request.content))
                .andExpect(jsonPath("$.author").value(article.author))
        }

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
                .perform(
                    MockMvcRequestBuilders
                        .patch("/api/v1/articles/${article.id}")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request)),
                ).andExpect(status().isBadRequest)
        }

        @Test
        fun `should delete a article`() {
            // 게시글 삭제가 가능하다
            // given
            val article = dataGenerator.generateArticle()
            // when & then
            mvc
                .perform(
                    MockMvcRequestBuilders.delete("/api/v1/articles/${article.id}"),
                ).andExpect(status().isNoContent)
            mvc
                .perform(
                    MockMvcRequestBuilders.delete("/api/v1/articles/${article.id}"),
                ).andExpect(status().isNotFound)
        }

        @Test
        fun `should paginate posts using published_at and id as cursor`() {
            // publised_at과 id를 커서로 하여 게시판의 게시글을 페이지네이션 함
            // given
            val board = dataGenerator.generateBoard()
            repeat(30) {
                dataGenerator.generateArticle(board = board)
            }
            // when & then
            val response =
                mvc
                    .perform(
                        MockMvcRequestBuilders.get("/api/v1/boards/${board.id!!}/articles?limit=15"),
                    ).andExpect(status().isOk)
                    .andExpect(jsonPath("$.paging.hasNext").value(true))
                    .andReturn()
                    .response
                    .getContentAsString(Charsets.UTF_8)
                    .let {
                        mapper.readValue(it, ArticlePagingResponse::class.java)
                    }
            assertArticlesAreSorted(response.data)
            assertArticleAreInBoard(response.data, board)

            val nextResponse =
                mvc
                    .perform(
                        MockMvcRequestBuilders
                            .get("/api/v1/boards/${board.id!!}/articles")
                            .param("limit", "15")
                            .param("nextPublishedAt", response.paging.nextPublishedAt!!.toString())
                            .param("nextId", response.paging.nextId!!.toString())
                            .accept(MediaType.APPLICATION_JSON),
                    ).andExpect(status().isOk)
                    .andExpect(jsonPath("$.paging.hasNext").value(false))
                    .andReturn()
                    .response
                    .getContentAsString(Charsets.UTF_8)
                    .let { mapper.readValue(it, ArticlePagingResponse::class.java) }
            assertArticlesAreSorted(nextResponse.data)
            assertArticleAreInBoard(nextResponse.data, board)
            assertTrue((response.data.map { it.id } + nextResponse.data.map { it.id }).toSet().size == 30)
        }

        @Test
        fun `Only two queries are fired during pagination`() {
            // 쿼리는 두번만 나간다
            // given
            val board = dataGenerator.generateBoard()
            val articles =
                List(10) {
                    dataGenerator.generateArticle(board = board)
                }
            val response =
                queryCounter.assertQueryCount(2) {
                    mvc
                        .perform(
                            MockMvcRequestBuilders.get("/api/v1/boards/${board.id!!}/articles?limit=20"),
                        ).andExpect(status().isOk)
                        .andReturn()
                        .response
                        .getContentAsString(Charsets.UTF_8)
                        .let {
                            mapper.readValue(it, ArticlePagingResponse::class.java)
                        }
                }
            assertArticlesAreSame(response.data, articles, board)
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

        private fun assertArticleAreInBoard(
            posts: List<ArticleDto>,
            board: Board,
        ) {
            posts.forEach {
                assertTrue(it.board.id == board.id)
            }
        }

        private fun assertArticlesAreSame(
            targetArticles: List<ArticleDto>,
            originalArticles: List<Article>,
            board: Board,
        ) {
            val expected =
                originalArticles
                    .map { ArticleDto(it, board) }
                    .sortedWith(
                        compareByDescending<ArticleDto> { it.publishedAt }
                            .thenByDescending { it.id },
                    )

            assertTrue(expected == targetArticles)
        }
    }

package com.wafflestudio.team2server

import com.wafflestudio.team2server.crawler.repository.CrawlerRepository
import com.wafflestudio.team2server.crawler.service.MysnuCrawlerService
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given
import org.mockito.Mockito.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.testcontainers.junit.jupiter.Testcontainers

@SpringBootTest
@ActiveProfiles("test")
@Testcontainers
@AutoConfigureMockMvc
class CrawlerTests
    @Autowired
    constructor(
        private val mvc: MockMvc,
        private val crawlerRepository: CrawlerRepository,
    ) {
        @MockitoBean
        private lateinit var mysnuCrawlerService: MysnuCrawlerService

        @Test
        fun `succeed on calling crawler service`() {
            given(mysnuCrawlerService.code).willReturn("mysnu")

            mvc
                .perform(
                    post("/api/crawlers/mysnu/run"),
                ).andExpect(status().isOk)

            verify(mysnuCrawlerService).crawl()
        }

        @Test
        fun `get crawler status returns ok and correct body structure`() {
            mvc
                .perform(
                    get("/api/crawlers"),
                ).andDo(print())
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.count").isNumber)
                .andExpect(jsonPath("$.results").isArray)
                .andExpect(jsonPath("$.results[0].boardName").exists())
        }
    }

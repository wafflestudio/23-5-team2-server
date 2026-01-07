package com.wafflestudio.team2server

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
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.testcontainers.junit.jupiter.Testcontainers

@SpringBootTest
@ActiveProfiles("test")
@Testcontainers
@AutoConfigureMockMvc
class ApplicationTests
    @Autowired
    constructor(
        private val mvc: MockMvc,
    ) {
        @MockitoBean
        private lateinit var mysnuCrawlerService: MysnuCrawlerService

        @Test
        fun swagger() {
            // swagger 정상 작동한다
            mvc
                .perform(
                    get("/swagger-ui/index.html"),
                ).andExpect(status().isOk)
        }

        @Test
        fun `크롤러 수동 실행 성공`() {
            given(mysnuCrawlerService.code).willReturn("mysnu")

            mvc
                .perform(
                    post("/api/crawlers/mysnu/run"),
                ).andExpect(status().isOk)

            verify(mysnuCrawlerService).crawl()
        }
    }

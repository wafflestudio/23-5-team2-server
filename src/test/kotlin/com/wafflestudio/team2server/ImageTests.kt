package com.wafflestudio.team2server

import com.fasterxml.jackson.databind.ObjectMapper
import com.wafflestudio.team2server.helper.DataGenerator
import com.wafflestudio.team2server.image.dto.CreateImageResponse
import jakarta.servlet.http.Cookie
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.http.MediaType
import org.springframework.mock.web.MockMultipartFile
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.delete
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.multipart
import org.testcontainers.junit.jupiter.Testcontainers

@SpringBootTest
@ActiveProfiles("test")
@Testcontainers
@AutoConfigureMockMvc
class ImageTests
    @Autowired
    constructor(
        private val mvc: MockMvc,
        private val mapper: ObjectMapper,
        private val dataGenerator: DataGenerator,
    ) {
        @Test
        fun `upload, fetch, and delete lifecycle test`() {
            // 1. Given: Setup User and Image content
            val (_, token) = dataGenerator.generateUser()
            val (_, token2) = dataGenerator.generateUser()
            val imageContent = "fake-binary-data-12345".toByteArray()
            val mockFile =
                MockMultipartFile(
                    "image",
                    "test-lifecycle.jpg",
                    MediaType.IMAGE_JPEG_VALUE,
                    imageContent,
                )

            // 2. When: Upload Image
            val uploadResult =
                mvc
                    .multipart("/api/v1/images") {
                        file(mockFile)
                        cookie(Cookie("AUTH-TOKEN", token))
                    }.andExpect {
                        status { isCreated() }
                        jsonPath("$.url") { exists() }
                    }.andReturn()

            val responseBody = uploadResult.response.contentAsString
            val createdImage = mapper.readValue(responseBody, CreateImageResponse::class.java)
            val imageUrl = createdImage.url

            // 3. When: User 2 tries to delete User 1's image
            mvc
                .delete("/api/v1/images") {
                    param("url", imageUrl)
                    cookie(Cookie("AUTH-TOKEN", token2)) // User 2's token
                }.andExpect {
                    status { isForbidden() } // Should return 403
                }

            // 4. When: Delete the image
            mvc
                .delete("/api/v1/images") {
                    param("url", imageUrl)
                    cookie(Cookie("AUTH-TOKEN", token))
                }.andExpect {
                    status { isNoContent() }
                }

            // 5. Then: Fetching the URL again should fail (404)
            mvc
                .get(imageUrl) {
                    cookie(Cookie("AUTH-TOKEN", token))
                }.andExpect {
                    status { isNotFound() }
                }
        }

        @Test
        fun `uploadImage returns 401 when user is not authenticated`() {
            // Given
            val mockFile =
                MockMultipartFile(
                    "image",
                    "test.png",
                    MediaType.IMAGE_PNG_VALUE,
                    "data".toByteArray(),
                )

            // When & Then: No Cookie provided
            mvc
                .multipart("/api/v1/images") {
                    file(mockFile)
                }.andExpect {
                    status { isUnauthorized() }
                }
        }
    }

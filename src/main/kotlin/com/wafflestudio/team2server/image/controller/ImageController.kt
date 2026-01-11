package com.wafflestudio.team2server.image.controller

import com.wafflestudio.team2server.image.dto.CreateImageResponse
import com.wafflestudio.team2server.image.service.ImageService
import com.wafflestudio.team2server.user.LoggedInUser
import com.wafflestudio.team2server.user.model.User
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/api/v1/images")
@Tag(name = "Image", description = "이미지 API")
class ImageController(
    private val imageService: ImageService,
) {
    @Operation(summary = "이미지 업로드", description = "이미지를 업로드하고 S3 URL 등을 반환합니다")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "201", description = "이미지 업로드 성공"),
            ApiResponse(responseCode = "400", description = "유효하지 않은 이미지"),
            ApiResponse(responseCode = "401", description = "사용자 인증 실패 (유효하지 않은 토큰)"),
            ApiResponse(responseCode = "413", description = "파일 크기 초과"),
            ApiResponse(responseCode = "500", description = "AWS S3 업로드 실패"),
        ],
    )
    @PostMapping
    fun uploadImage(
        @Parameter(hidden = true) @LoggedInUser user: User,
        @RequestParam("image") image: MultipartFile,
    ): ResponseEntity<CreateImageResponse> {
        val metadata = imageService.createImage(user.id!!, image)
        return ResponseEntity.status(HttpStatus.CREATED).body(metadata)
    }

    @Operation(summary = "이미지 삭제", description = "해당 S3 url 이미지를 S3 및 image_metadata 테이블에서 삭제합니다")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "204", description = "이미지 삭제 성공"),
            ApiResponse(responseCode = "401", description = "사용자 인증 실패 (유효하지 않은 토큰)"),
            ApiResponse(responseCode = "403", description = "본인이 업로드한 이미지가 아님"),
            ApiResponse(responseCode = "404", description = "존재하지 않는 이미지"),
            ApiResponse(responseCode = "500", description = "AWS S3 삭제 실패"),
        ],
    )
    @DeleteMapping
    fun deleteImage(
        @Parameter(hidden = true) @LoggedInUser user: User,
        @RequestParam("url") url: String,
    ): ResponseEntity<Unit> {
        imageService.deleteImage(user.id!!, url)
        return ResponseEntity.noContent().build()
    }
}

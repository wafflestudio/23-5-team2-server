package com.wafflestudio.team2server.article.controller

import com.wafflestudio.team2server.article.dto.ArticlePagingRequest
import com.wafflestudio.team2server.article.dto.UpdateArticleRequest
import com.wafflestudio.team2server.article.dto.core.ArticleDto
import com.wafflestudio.team2server.article.dto.request.CreateArticleRequest
import com.wafflestudio.team2server.article.dto.response.ArticlePagingResponse
import com.wafflestudio.team2server.article.dto.response.CreateArticleResponse
import com.wafflestudio.team2server.article.service.ArticleService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import java.time.Instant

@RestController
@Tag(name = "Article", description = "게시글 관리 API")
class ArticleController(
    private val articleService: ArticleService,
) {
    @Operation(summary = "게시글 생성", description = "게시판에 글이 작성되는지 확인.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "201", description = "게시글 생성"),
            ApiResponse(responseCode = "400", description = "잘못된 요청(제목, 글쓴이, 원본링크, 글 작성시간 중 하나가 작성되지 않음)"),
            ApiResponse(responseCode = "404", description = "게시판을 찾을 수 없음"),
        ],
    )
    @PostMapping("/api/v1/boards/{boardId}/articles")
    fun create(
        @Parameter(
            description = "게시판 ID",
            example = "1",
        ) @PathVariable boardId: Long,
        @RequestBody createArticleRequest: CreateArticleRequest,
    ): ResponseEntity<CreateArticleResponse> {
        val articleDto =
            articleService.create(
                content = createArticleRequest.content,
                title = createArticleRequest.title,
                author = createArticleRequest.author,
                originLink = createArticleRequest.originLink,
                publihedAt = createArticleRequest.publishedAt,
                boardId = boardId,
            )
        return ResponseEntity.ok(articleDto)
    }

    @Operation(
        summary = "게시글 목록 조회",
        description = """
            게시판의 게시글을 페이지네이션 해서 가져옴
            커서 기반 페이지 네이션 사용
            정렬: 게시글 생성시간 내림차순
        """,
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "게시글 목록 조회 성공"),
            ApiResponse(responseCode = "404", description = "게시판을 찾을 수 없음"),
        ],
    )
    @GetMapping("/api/v1/boards/{boardId}/articles")
    fun paging(
        @Parameter(description = "게시판 ID", example = "1")
        @PathVariable boardId: Long,
        @RequestBody articlePagingRequest: ArticlePagingRequest,
    ): ResponseEntity<ArticlePagingResponse> {
        val articlePagingResponse =
            articleService.pageByBoardId(
                boardId = boardId,
                nextPublishedAt = articlePagingRequest.nextPublishedAt?.let { Instant.ofEpochMilli(it) },
                nextId = articlePagingRequest.nextId,
                limit = articlePagingRequest.limit,
            )
        return ResponseEntity.ok(articlePagingResponse)
    }

    @Operation(summary = "특정 게시글 조회", description = "게시글 ID로 게시글 상세 정보 조회")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "특정 게시글 조회 성공"),
            ApiResponse(responseCode = "404", description = "게시판을 찾을 수 없습니다."),
        ],
    )
    @GetMapping("/api/v1/articles/{articleId}")
    fun get(
        @Parameter(
            description = "게시글 ID",
            example = "1",
        ) @PathVariable articleId: Long,
    ): ResponseEntity<ArticleDto> {
        val articleDto = articleService.get(articleId)
        return ResponseEntity.ok(articleDto)
    }

    @Operation(summary = "특정 게시글 업데이트", description = "게시글 ID로 게시글 상세 정보 업데이트")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "특정 게시글 업데이트 성공"),
            ApiResponse(responseCode = "400", description = "잘못된 요청(제목, 글쓴이, 원본링크, 글 작성시간 중 하나가 작성되지 않음)"),
            ApiResponse(responseCode = "404", description = "게시글을 찾을 수 없습니다."),
        ],
    )
    @PatchMapping("/api/v1/articles/{articleId}")
    fun update(
        @Parameter(
            description = "게시글 ID",
            example = "1",
        )
        @PathVariable articleId: Long,
        @RequestBody updateArticleRequest: UpdateArticleRequest,
    ): ResponseEntity<ArticleDto> {
        val articleDto =
            articleService.update(
                articleId = articleId,
                content = updateArticleRequest.content,
                author = updateArticleRequest.author,
                originLink = updateArticleRequest.originLink,
                title = updateArticleRequest.title,
                publishedAt = updateArticleRequest.publishedAt,
            )
        return ResponseEntity.ok(articleDto)
    }

    @Operation(summary = "특정 게시글 삭제", description = "게시글 ID로 게시글 삭제")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "204", description = "특정 게시글 삭제 성공"),
            ApiResponse(responseCode = "404", description = "게시글을 찾을 수 없습니다."),
        ],
    )
    @DeleteMapping("/api/v1/articles/{articleId}")
    fun delete(
        @Parameter(
            description = "게시글 ID",
            example = "1",
        )
        @PathVariable articleId: Long,
    ): ResponseEntity<Unit> {
        articleService.delete(articleId)
        return ResponseEntity.noContent().build()
    }
}

package com.wafflestudio.team2server.board.controller

import com.wafflestudio.team2server.board.dto.BoardsResponse
import com.wafflestudio.team2server.board.service.BoardService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Board", description = "게시글 관리 API")
class BoardController(
    private val boardService: BoardService,
) {
    @Operation(summary = "게시판 목록 조회", description = "전체 게시판 목록을 조회.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "조회 성공"),
        ],
    )
    @GetMapping("/boards")
    fun getBoards(): ResponseEntity<BoardsResponse> = ResponseEntity.ok(BoardsResponse(boardService.getBoards()))
}

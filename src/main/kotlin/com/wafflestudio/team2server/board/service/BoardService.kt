package com.wafflestudio.team2server.board.service

import com.wafflestudio.team2server.board.dto.core.BoardDto
import com.wafflestudio.team2server.board.repository.BoardRepository
import org.springframework.stereotype.Service

@Service
class BoardService(
    private val boardRepository: BoardRepository,
) {
    fun getBoards(): List<BoardDto> = boardRepository.findAll().map { BoardDto(it) }
}

package com.wafflestudio.team2server.board.dto.core

import com.wafflestudio.team2server.board.model.Board

data class BoardDto(
    val id: Long,
    val name: String,
    val sourceUrl: String?,
) {
    constructor(board: Board) : this(
        id = board.id!!,
        name = board.name,
        sourceUrl = board.sourceUrl,
    )
}

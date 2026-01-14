package com.wafflestudio.team2server.board.repository

import com.wafflestudio.team2server.board.model.Board
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.ListCrudRepository
import org.springframework.data.repository.query.Param

interface BoardRepository : ListCrudRepository<Board, Long> {
    @Query(
        """
        SELECT id
        FROM boards
        WHERE id IN (:ids)
        """,
    )
    fun findExistingIds(
        @Param("ids") ids: List<Long>,
    ): List<Long>
}

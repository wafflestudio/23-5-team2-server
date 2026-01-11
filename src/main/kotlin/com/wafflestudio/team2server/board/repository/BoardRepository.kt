package com.wafflestudio.team2server.board.repository

import com.wafflestudio.team2server.board.model.Board
import org.springframework.data.repository.ListCrudRepository

interface BoardRepository : ListCrudRepository<Board, Long>

package com.wafflestudio.team2server.helper

import com.wafflestudio.team2server.article.model.Article
import com.wafflestudio.team2server.article.repository.ArticleRepository
import com.wafflestudio.team2server.board.model.Board
import com.wafflestudio.team2server.board.repository.BoardRepository
import com.wafflestudio.team2server.user.JwtProvider
import com.wafflestudio.team2server.user.model.User
import com.wafflestudio.team2server.user.repository.UserRepository
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Component
import java.time.Instant
import java.util.UUID
import kotlin.random.Random

@Component
class DataGenerator(
    private val userRepository: UserRepository,
    private val jwtProvider: JwtProvider,
    private val bcryptPasswordEncoder: BCryptPasswordEncoder,
    private val boardRepository: BoardRepository,
    private val articleRepository: ArticleRepository,
) {
    fun generateUser(
        userId: String? = null,
        password: String? = null,
    ): Pair<User, String> {
        val user =
            userRepository.save(
                User(
                    localId = userId ?: "user-${Random.nextInt(1000000)}",
                    password = bcryptPasswordEncoder.encode(password ?: "password-${Random.nextInt(1000000)}"),
                ),
            )
        return user to jwtProvider.createToken(user.id!!)
    }

    fun generateBoard(
        name: String? = null,
        sorceUrl: String? = null,
    ): Board {
        val board =
            boardRepository.save(
                Board(
                    name = name ?: "board-${Random.nextInt(1000000)}",
                    sourceUrl = sorceUrl ?: "https://example.com/${UUID.randomUUID()}",
                ),
            )
        return board
    }

    fun generateArticle(
        title: String? = null,
        content: String? = null,
        publishedAt: Instant = Instant.now(),
        author: String? = null,
        orginalLink: String? = null,
    ): Article {
        val article =
            articleRepository.save(
                Article(
                    title = title ?: "article-${Random.nextInt(1000000)}",
                    content = content ?: "content-${Random.nextInt(1000000)}",
                    author = author ?: "author-${Random.nextInt(1000000)}",
                    publishedAt = publishedAt,
                    originLink = orginalLink ?: "https://example.com/${UUID.randomUUID()}",
                    boardId = 1,
                ),
            )
        return article
    }
}

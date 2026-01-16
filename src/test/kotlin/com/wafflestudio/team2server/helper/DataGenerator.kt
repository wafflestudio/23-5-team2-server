package com.wafflestudio.team2server.helper

import com.wafflestudio.team2server.article.model.Article
import com.wafflestudio.team2server.article.service.ArticleService
import com.wafflestudio.team2server.board.repository.BoardRepository
import com.wafflestudio.team2server.inbox.model.Inbox
import com.wafflestudio.team2server.inbox.repository.InboxRepository
import com.wafflestudio.team2server.subscription.model.Subscription
import com.wafflestudio.team2server.subscription.repository.SubscriptionRepository
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
    private val articleService: ArticleService,
    private val subscriptionRepository: SubscriptionRepository,
    private val inboxRepository: InboxRepository,
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

    fun generateArticle(
        title: String? = null,
        content: String? = null,
        publishedAt: Instant = Instant.now(),
        author: String? = null,
        originLink: String? = null,
    ): Article {
        val article =
            articleService.saveNewArticle(
                Article(
                    title = title ?: "article-${Random.nextInt(1000000)}",
                    content = content ?: "content-${Random.nextInt(1000000)}",
                    author = author ?: "author-${Random.nextInt(1000000)}",
                    publishedAt = publishedAt,
                    originLink = originLink ?: "https://example.com/${UUID.randomUUID()}",
                    boardId = 1,
                ),
            )
        return article
    }

    fun generateSubscription(
        userId: Long,
        boardId: Long,
    ): Subscription = subscriptionRepository.save(Subscription(userId = userId, boardId = boardId))

    fun generateInbox(
        userId: Long,
        articleTitle: String?,
    ): Inbox {
        val article = generateArticle(title = articleTitle)
        return inboxRepository.save(Inbox(userId = userId, articleId = article.id!!))
    }
}

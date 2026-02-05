package com.wafflestudio.team2server.email.service

import com.wafflestudio.team2server.article.model.Article
import jakarta.annotation.PostConstruct
import jakarta.mail.internet.MimeMessage
import org.slf4j.LoggerFactory
import org.springframework.core.env.Environment
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Service
class MailService(
    private val javaMailSender: JavaMailSender,
    private val environment: Environment,
) {
    private val logger = LoggerFactory.getLogger(MailService::class.java)

    @PostConstruct
    fun logMailConfiguration() {
        val host = environment.getProperty("spring.mail.host") ?: "not set"
        val port = environment.getProperty("spring.mail.port") ?: "not set"
        val username = environment.getProperty("spring.mail.username") ?: "not set"
        val hasPassword = !environment.getProperty("spring.mail.password").isNullOrBlank()

        logger.info(
            "이메일 설정 초기화: host={}, port={}, username={}, password={}",
            host,
            port,
            username,
            if (hasPassword) "설정됨" else "설정되지 않음",
        )

        if (!hasPassword || username == "not set") {
            logger.warn("이메일 설정이 완전하지 않습니다. MAIL_USERNAME과 MAIL_PASSWORD 환경 변수를 확인하세요.")
        }
    }

    @Async
    fun sendArticleNotification(
        email: String,
        article: Article,
    ) {
        logger.info("이메일 발송 시작: 수신자={}, 제목={}, 게시글ID={}", email, article.title, article.id)
        val subject = "[새 글 알림] ${article.title}"
        val htmlBody = createHtmlBody(article)

        sendHtmlEmail(email, subject, htmlBody)
    }

    fun sendHtmlEmail(
        to: String,
        subject: String,
        htmlBody: String,
    ) {
        try {
            logger.debug("이메일 메시지 생성 시작: 수신자={}, 제목={}", to, subject)
            val message: MimeMessage = javaMailSender.createMimeMessage()
            val helper = MimeMessageHelper(message, true, "UTF-8")

            helper.setTo(to)
            helper.setSubject(subject)
            helper.setText(htmlBody, true)

            logger.debug("이메일 전송 시도: 수신자={}, 제목={}", to, subject)
            javaMailSender.send(message)
            logger.info("이메일 발송 성공: 수신자={}, 제목={}", to, subject)
        } catch (e: jakarta.mail.AuthenticationFailedException) {
            logger.error("이메일 인증 실패: 수신자={}, 제목={}, 오류={}", to, subject, e.message, e)
        } catch (e: jakarta.mail.MessagingException) {
            logger.error("이메일 메시징 오류: 수신자={}, 제목={}, 오류={}", to, subject, e.message, e)
        } catch (e: java.net.SocketTimeoutException) {
            logger.error("이메일 전송 타임아웃: 수신자={}, 제목={}, 오류={}", to, subject, e.message, e)
        } catch (e: java.net.ConnectException) {
            logger.error("이메일 서버 연결 실패: 수신자={}, 제목={}, 오류={}", to, subject, e.message, e)
        } catch (e: Exception) {
            logger.error("이메일 발송 실패: 수신자={}, 제목={}, 오류타입={}, 오류={}", to, subject, e.javaClass.simpleName, e.message, e)
        }
    }

    private fun createHtmlBody(article: Article): String {
        val formatter =
            DateTimeFormatter
                .ofPattern("yyyy년 MM월 dd일 HH:mm")
                .withZone(ZoneId.of("Asia/Seoul"))

        val dateString = formatter.format(article.publishedAt)
        val fullContentHtml = article.content ?: ""

        val linkHtml =
            if (!article.originLink.isNullOrBlank()) {
                """
        <div style="text-align: center; margin-top: 40px; margin-bottom: 20px;">
            <a href="${article.originLink}" 
               style="background-color: #2c3e50; color: white; padding: 14px 20px; text-decoration: none; border-radius: 4px; font-weight: bold; font-size: 16px; display: inline-block;">
               🌐 웹사이트에서 원본 보기
            </a>
        </div>
        """
            } else {
                ""
            }

        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
            </head>
            <body style="margin: 0; padding: 0; font-family: 'Apple SD Gothic Neo', 'Malgun Gothic', sans-serif; background-color: #f4f4f4;">
                <div style="max-width: 100%; margin: 0 auto; background-color: #ffffff;">
                    
                    <div style="background-color: #2c3e50; padding: 25px 20px; text-align: center;">
                        <h1 style="color: #ffffff; margin: 0; font-size: 20px; font-weight: 600;">
                            새 글 알림
                        </h1>
                    </div>

                    <div style="padding: 30px 20px;">
                        <h2 style="color: #222; margin-top: 0; margin-bottom: 15px; font-size: 24px; line-height: 1.3; border-bottom: 2px solid #2c3e50; padding-bottom: 15px;">
                            ${article.title}
                        </h2>
                        
                        <div style="font-size: 13px; color: #666; margin-bottom: 30px; text-align: right;">
                            <span style="font-weight: bold; color: #333;">${article.author}</span> 
                            <span style="color: #ddd;">|</span> 
                            <span>$dateString</span>
                        </div>

                        <div style="font-size: 15px; line-height: 1.7; color: #333; word-break: break-word;">
                            $fullContentHtml
                        </div>

                        $linkHtml
                    </div>

                    <div style="background-color: #eee; padding: 20px; text-align: center; font-size: 12px; color: #888; border-top: 1px solid #ddd;">
                        <p style="margin: 0;">본 메일은 회원님이 구독 중인 게시판의 새 글 알림입니다.</p>
                    </div>
                </div>
            </body>
            </html>
            """.trimIndent()
    }
}

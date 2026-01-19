package com.wafflestudio.team2server.email.service

import com.wafflestudio.team2server.article.model.Article
import jakarta.mail.internet.MimeMessage
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Service
class MailService(
    private val javaMailSender: JavaMailSender,
) {
    @Async
    fun sendArticleNotification(
        email: String,
        article: Article,
    ) {
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
            val message: MimeMessage = javaMailSender.createMimeMessage()
            val helper = MimeMessageHelper(message, true, "UTF-8")

            helper.setTo(to)
            helper.setSubject(subject)
            helper.setText(htmlBody, true)

            javaMailSender.send(message)
        } catch (e: Exception) {
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

package com.wafflestudio.team2server.user

import com.wafflestudio.team2server.config.HttpCookieOAuth2AuthorizationRequestRepository
import com.wafflestudio.team2server.user.repository.UserRepository
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.security.core.Authentication
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler
import org.springframework.stereotype.Component

@Component
class OAuth2SuccessHandler(
    private val userRepository: UserRepository,
    private val jwtProvider: JwtProvider,
    private val cookieRepository: HttpCookieOAuth2AuthorizationRequestRepository,
    @Value("\${app.frontend.url}") private val frontendUrl: List<String>,
) : SimpleUrlAuthenticationSuccessHandler() {
    override fun onAuthenticationSuccess(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authentication: Authentication,
    ) {
        val oAuth2User = authentication.principal as OAuth2User
        val email =
            oAuth2User.getAttribute<String?>(
                "email",
            ) ?: throw IllegalStateException("OAuth2 provider did not supply a non-null 'email' attribute")
        val user = userRepository.findByOauthId(email) ?: throw IllegalStateException("User not found for OAuth id (email=$email)")

        val token = jwtProvider.createToken(user.id!!)
        val jwtCookie = jwtProvider.createJwtCookie(token)
        response.addHeader(HttpHeaders.SET_COOKIE, jwtCookie.toString())

        val rawTargetUrl =
            request.cookies
                ?.find { it.name == HttpCookieOAuth2AuthorizationRequestRepository.REDIRECT_URI_PARAM_COOKIE_NAME }
                ?.value
        val targetUrl = if (rawTargetUrl != null && isValidRedirectUrl(rawTargetUrl)) rawTargetUrl else "/"

        // IMPORTANT: Clear the cookies used for the OAuth flow
        cookieRepository.deleteCookies(request, response)

        redirectStrategy.sendRedirect(request, response, targetUrl)
    }

    private fun isValidRedirectUrl(url: String): Boolean = frontendUrl.find { url.startsWith(it) } != null
}

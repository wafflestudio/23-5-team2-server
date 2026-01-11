package com.wafflestudio.team2server.user

import com.wafflestudio.team2server.config.HttpCookieOAuth2AuthorizationRequestRepository
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler
import org.springframework.stereotype.Component
import org.springframework.web.util.UriComponentsBuilder

@Component
class OAuth2FailureHandler(
    private val cookieRepository: HttpCookieOAuth2AuthorizationRequestRepository,
) : SimpleUrlAuthenticationFailureHandler() {
    override fun onAuthenticationFailure(
        request: HttpServletRequest,
        response: HttpServletResponse,
        exception: AuthenticationException,
    ) {
        val targetUrl =
            request.cookies
                ?.find { it.name == HttpCookieOAuth2AuthorizationRequestRepository.REDIRECT_URI_PARAM_COOKIE_NAME }
                ?.value ?: "/"

        val redirectUrl =
            UriComponentsBuilder
                .fromUriString(targetUrl)
                .queryParam("error", "login_failed")
                .build()
                .toUriString()

        cookieRepository.deleteCookies(request, response)
        redirectStrategy.sendRedirect(request, response, redirectUrl)
    }
}

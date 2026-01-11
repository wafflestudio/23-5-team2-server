package com.wafflestudio.team2server.config

import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest
import org.springframework.stereotype.Component
import org.springframework.util.SerializationUtils
import java.io.ByteArrayInputStream
import java.io.ObjectInputStream
import java.util.Base64

object CookieUtils {
    fun serialize(obj: Any): String =
        Base64
            .getUrlEncoder()
            .encodeToString(SerializationUtils.serialize(obj))

    fun <T> deserialize(
        cookie: Cookie,
        cls: Class<T>,
    ): T {
        val decodedBytes = Base64.getUrlDecoder().decode(cookie.value)
        return ByteArrayInputStream(decodedBytes).use { bis ->
            ObjectInputStream(bis).use { ois ->
                cls.cast(ois.readObject())
            }
        }
    }
}

@Component
class HttpCookieOAuth2AuthorizationRequestRepository : AuthorizationRequestRepository<OAuth2AuthorizationRequest> {
    companion object {
        const val OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME = "oauth2_auth_request"
        const val REDIRECT_URI_PARAM_COOKIE_NAME = "redirect_uri"
        private const val COOKIE_EXPIRE_SECONDS = 180
    }

    override fun loadAuthorizationRequest(request: HttpServletRequest): OAuth2AuthorizationRequest? {
        val cookie = request.cookies?.find { it.name == OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME }
        return cookie?.let { CookieUtils.deserialize(it, OAuth2AuthorizationRequest::class.java) }
    }

    override fun saveAuthorizationRequest(
        authorizationRequest: OAuth2AuthorizationRequest?,
        request: HttpServletRequest,
        response: HttpServletResponse,
    ) {
        if (authorizationRequest == null) {
            deleteCookies(request, response)
            return
        }

        // 1. Save the actual OAuth2 Request (contains state, nonce, etc.)
        val authRequestCookie =
            Cookie(OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME, CookieUtils.serialize(authorizationRequest)).apply {
                path = "/"
                isHttpOnly = true
                maxAge = COOKIE_EXPIRE_SECONDS
            }
        response.addCookie(authRequestCookie)

        // 2. Save the target frontend URL
        val redirectUriAfterLogin = request.getParameter(REDIRECT_URI_PARAM_COOKIE_NAME)
        if (!redirectUriAfterLogin.isNullOrBlank()) {
            val redirectCookie =
                Cookie(REDIRECT_URI_PARAM_COOKIE_NAME, redirectUriAfterLogin).apply {
                    path = "/"
                    isHttpOnly = true
                    maxAge = COOKIE_EXPIRE_SECONDS
                }
            response.addCookie(redirectCookie)
        }
    }

    override fun removeAuthorizationRequest(
        request: HttpServletRequest,
        response: HttpServletResponse,
    ): OAuth2AuthorizationRequest? {
        val originalRequest = this.loadAuthorizationRequest(request)
        // We don't delete cookies here because we still need 'redirect_uri' in the SuccessHandler
        return originalRequest
    }

    fun deleteCookies(
        request: HttpServletRequest,
        response: HttpServletResponse,
    ) {
        request.cookies?.forEach { cookie ->
            if (cookie.name == OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME || cookie.name == REDIRECT_URI_PARAM_COOKIE_NAME) {
                cookie.value = ""
                cookie.path = "/"
                cookie.maxAge = 0
                response.addCookie(cookie)
            }
        }
    }
}

package com.wafflestudio.team2server.user

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.ResponseCookie
import org.springframework.stereotype.Component
import java.util.Date

@Component
class JwtProvider(
    @Value("\${jwt.secret}")
    private val secretKey: String,
    @Value("\${jwt.expiration-in-ms}")
    private val expirationInMs: Long,
) {
    private val key = Keys.hmacShaKeyFor(secretKey.toByteArray())

    fun createToken(userId: Long): String {
        val now = Date()
        val validity = Date(now.time + expirationInMs)

        return Jwts
            .builder()
            .setSubject(userId.toString())
            .setIssuedAt(now)
            .setExpiration(validity)
            .signWith(key, SignatureAlgorithm.HS256)
            .compact()
    }

    fun getUserId(token: String): Long =
        Jwts
            .parserBuilder()
            .setSigningKey(key)
            .build()
            .parseClaimsJws(token)
            .body
            .subject
            .toLong()

    fun validateToken(token: String): Boolean {
        try {
            Jwts
                .parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
            return true
        } catch (_: Exception) {
            // do nothing
        }
        return false
    }

    fun getExpiration(token: String): Long {
        val claims =
            Jwts
                .parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .body
        return claims.expiration.time
    }

    fun createJwtCookie(token: String): ResponseCookie =
        ResponseCookie
            .from("AUTH-TOKEN", token)
            .httpOnly(true) // Prevents JS access (XSS protection)
            .path("/") // Available for all routes
            .maxAge((getExpiration(token) - System.currentTimeMillis()) / 1000)
            .sameSite("None")
            .secure(true)
            .build()
}

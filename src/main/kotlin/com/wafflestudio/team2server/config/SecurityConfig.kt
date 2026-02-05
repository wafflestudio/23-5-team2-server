package com.wafflestudio.team2server.config

import com.wafflestudio.team2server.user.OAuth2FailureHandler
import com.wafflestudio.team2server.user.OAuth2SuccessHandler
import com.wafflestudio.team2server.user.service.GoogleOAuth2UserService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.web.SecurityFilterChain

@EnableWebSecurity
@Configuration
class SecurityConfig(
    private val googleOAuth2UserService: GoogleOAuth2UserService,
    private val oAuth2SuccessHandler: OAuth2SuccessHandler,
    private val oAuth2FailureHandler: OAuth2FailureHandler,
    private val cookieRepository: HttpCookieOAuth2AuthorizationRequestRepository,
) {
    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        // it does not use authorizeHttpRequests
        // requiring authorization is handled at the controller level
        // when use @LoggedInUser, if there is no authorization, UserArgumentResolver will return 401.
        http
            .csrf { it.disable() }
            .httpBasic { it.disable() }
            .formLogin { it.disable() }
            .oauth2Login { oauth2 ->
                oauth2
                    .authorizationEndpoint { it.authorizationRequestRepository(cookieRepository) }
                    .userInfoEndpoint { it.userService(googleOAuth2UserService) }
                    .successHandler(oAuth2SuccessHandler)
                    .failureHandler(oAuth2FailureHandler)
            }
        return http.build()
    }

    @Bean
    fun bcryptPasswordEncoder(): BCryptPasswordEncoder = BCryptPasswordEncoder()
}

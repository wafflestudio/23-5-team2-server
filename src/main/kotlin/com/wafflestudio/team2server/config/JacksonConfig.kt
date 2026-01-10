package com.wafflestudio.team2server.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class JacksonConfig {
    @Bean
    fun objectMapper(): ObjectMapper =
        ObjectMapper()
            .registerKotlinModule() // Essential for Kotlin data classes
            .registerModule(JavaTimeModule()) // Fixes the Instant/LocalDateTime issue
            .disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
}

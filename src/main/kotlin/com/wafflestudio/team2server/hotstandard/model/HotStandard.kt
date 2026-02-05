package com.wafflestudio.team2server.hotstandard.model

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

@Table("hotstandards")
data class HotStandard(
    @Id
    val id: Long? = null,
    var hotScore: Long,
    var viewsWeight: Double,
)

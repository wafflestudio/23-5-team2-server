package com.wafflestudio.team2server.user.dto

data class UpdateLocalRequest(
    val oldPassword: String,
    val newPassword: String,
)

package com.wafflestudio.team2server.image.dto.core

import com.wafflestudio.team2server.image.model.ImageMetadata
import java.time.Instant

data class ImageMetadataDto(
    val id: Long,
    val articleId: Long?,
    val url: String,
    val originalFilename: String,
    val createdAt: Instant,
) {
    constructor(meta: ImageMetadata) : this(meta.id!!, meta.articleId, meta.url, meta.originalFilename, meta.createdAt!!)
}

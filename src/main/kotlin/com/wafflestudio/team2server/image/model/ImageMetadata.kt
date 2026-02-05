package com.wafflestudio.team2server.image.model

import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.time.Instant

@Table("image_metadata")
class ImageMetadata(
    @Id val id: Long? = null,
    var authorId: Long? = null,
    var articleId: Long? = null,
    var url: String,
    var originalFilename: String,
    @CreatedDate
    var createdAt: Instant? = null,
)

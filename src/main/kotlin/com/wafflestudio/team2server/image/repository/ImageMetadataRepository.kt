package com.wafflestudio.team2server.image.repository

import com.wafflestudio.team2server.image.model.ImageMetadata
import org.springframework.data.repository.ListCrudRepository

interface ImageMetadataRepository : ListCrudRepository<ImageMetadata, Long> {
    fun findByUrl(url: String): ImageMetadata?
}

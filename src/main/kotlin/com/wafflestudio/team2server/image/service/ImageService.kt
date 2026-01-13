package com.wafflestudio.team2server.image.service

import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.model.ObjectMetadata
import com.wafflestudio.team2server.image.AwsS3FailedException
import com.wafflestudio.team2server.image.ImageDeletionForbiddenException
import com.wafflestudio.team2server.image.ImageNotFoundException
import com.wafflestudio.team2server.image.InvalidExtensionException
import com.wafflestudio.team2server.image.InvalidFileSizeException
import com.wafflestudio.team2server.image.InvalidFilenameException
import com.wafflestudio.team2server.image.InvalidMimeTypeException
import com.wafflestudio.team2server.image.dto.core.ImageMetadataDto
import com.wafflestudio.team2server.image.model.ImageMetadata
import com.wafflestudio.team2server.image.repository.ImageMetadataRepository
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.util.UUID

@Service
class ImageService(
    private val imageMetadataRepository: ImageMetadataRepository,
    private val amazonS3Client: AmazonS3Client,
    @Value("\${cloud.aws.s3.bucket}")
    private val bucket: String,
    @Value("\${cloud.aws.s3.directory}")
    private val directory: String,
) {
    val allowedExtensions = listOf("jpg", "jpeg", "png", "gif", "svg")
    val allowedSize = 10_000_000

    fun createImage(
        authorId: Long,
        image: MultipartFile,
    ): ImageMetadataDto {
        val originalFilename = image.originalFilename ?: throw InvalidFilenameException()
        val dotIndex = originalFilename.lastIndexOf(".")
        if (dotIndex == -1 || dotIndex == originalFilename.length - 1) {
            throw InvalidExtensionException()
        }
        val extension = originalFilename.substring(dotIndex + 1)
        if (!allowedExtensions.contains(extension)) {
            throw InvalidExtensionException()
        }
        val mimeType = image.contentType ?: throw InvalidMimeTypeException()
        if (!mimeType.startsWith("image/")) {
            throw InvalidMimeTypeException()
        }
        val fileSize = image.size
        if (fileSize > allowedSize) {
            throw InvalidFileSizeException()
        }
        val uuid = UUID.randomUUID().toString()
        val filename = "$directory/$uuid.$extension"
        val metadata = ObjectMetadata()
        metadata.contentType = mimeType
        metadata.contentLength = fileSize

        try {
            amazonS3Client.putObject(bucket, filename, image.inputStream, metadata)
            val url = amazonS3Client.getUrl(bucket, filename).toString()
            val imageMetadata =
                imageMetadataRepository.save(
                    ImageMetadata(authorId = authorId, url = url, originalFilename = originalFilename),
                )
            return ImageMetadataDto(imageMetadata)
        } catch (_: Exception) {
            throw AwsS3FailedException()
        }
    }

    fun deleteImage(
        userId: Long,
        url: String,
    ) {
        val imageMetadata = imageMetadataRepository.findByUrl(url) ?: throw ImageNotFoundException()
        if (imageMetadata.authorId != userId) {
            throw ImageDeletionForbiddenException()
        }
        val fileUrl = imageMetadata.url
        val splitStr = ".com/"
        val fileName = fileUrl.substring(fileUrl.lastIndexOf(splitStr) + splitStr.length)
        try {
            amazonS3Client.deleteObject(bucket, fileName)
            imageMetadataRepository.delete(imageMetadata)
        } catch (_: Exception) {
            throw AwsS3FailedException()
        }
    }
}

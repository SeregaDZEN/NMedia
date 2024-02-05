package ru.netology.nmedia.dto

data class Post(
    val id: Long,
    val content: String,
    val author: String,
    val authorAvatar: String = "",
    val published: Long,
    val likedByMe: Boolean,
    val hide: Boolean,
    val likes: Int,
    var attachment: Attachment? = null,
)

data class Attachment(
    val url: String,
    val description: String?,
    val type: String,
)

enum class AttachmentType {
    IMAGE
}


package ru.netology.nmedia.dto

sealed interface FeedItem {
    val id: Long
}

data class Post(
    override val id: Long,
    val authorId: Long,
    val content: String,
    val author: String,
    val authorAvatar: String = "",
    val published: Long,
    val likedByMe: Boolean,
    val hide: Boolean,
    val likes: Int,
    var attachment: Attachment? = null,
    val ownedByMe: Boolean = false
) : FeedItem

data class Ad(
    override val id: Long,
    val image: String
) : FeedItem

data class TimeCheck(
    val timeType: TimeType,
) : FeedItem {
    override val id: Long = timeType.ordinal.toLong()
}

enum class TimeType {
    TODAY,
    YESTERDAY,
    WEEK_AGO,
}

data class Attachment(
    val url: String,
    val type: AttachmentType,
)

enum class AttachmentType {
    IMAGE
}


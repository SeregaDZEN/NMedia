package ru.netology.nmedia.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import ru.netology.nmedia.dto.Attachment
import ru.netology.nmedia.dto.Post

@Entity
data class PostEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val content: String,
    val author: String,
    val authorAvatar: String,
    val published: Long,
    val likedByMe: Boolean,
    val likes: Int = 0,
    @Embedded
    val attachment: Attachment?,
) {
    fun toDto() = Post(id, content,author,authorAvatar, published,  likedByMe, likes,attachment)

    companion object {
        fun fromDto(dto: Post) =
            PostEntity(dto.id, dto.content,dto.author, dto.authorAvatar, dto.published, dto.likedByMe, dto.likes,dto.attachment)

    }
}


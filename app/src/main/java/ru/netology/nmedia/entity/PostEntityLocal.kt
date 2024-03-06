package ru.netology.nmedia.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import ru.netology.nmedia.dto.Attachment
import ru.netology.nmedia.dto.Post

@Entity

data class PostEntityLocal(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val authorId: Long,
    val content: String,
    val author: String,
    val authorAvatar: String,
    val published: Long,
    val likedByMe: Boolean,
    val hide: Boolean,
    val likes: Int = 0,

    @Embedded
    val attachment: Attachment?
) {
    fun toDto() = Post(id, authorId, content,author,authorAvatar, published,  likedByMe,hide, likes, attachment)

    companion object {
        fun fromDto(dto: Post) =
            PostEntityLocal( dto.id,dto.authorId, dto.content,dto.author, dto.authorAvatar, dto.published, dto.likedByMe,dto.hide, dto.likes,dto.attachment)

    }
}

fun List<PostEntityLocal>.toDto() = map { it.toDto() }

fun List<Post>.toEntityLocal()= map{ PostEntityLocal.fromDto(it)}


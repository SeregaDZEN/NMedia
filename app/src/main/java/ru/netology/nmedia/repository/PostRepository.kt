package ru.netology.nmedia.repository


import kotlinx.coroutines.flow.Flow
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.model.PhotoModel


interface PostRepository {


    suspend fun refreshHide()

    suspend fun showAll()

    val dataRepo: Flow<List<Post>>
    fun getNewerCount(id: Long): Flow<Int>
    suspend fun getAll()

    suspend fun save(post: Post, photo: PhotoModel?)
    suspend fun likeById(id: Long)
    suspend fun removeById(id: Long)


}



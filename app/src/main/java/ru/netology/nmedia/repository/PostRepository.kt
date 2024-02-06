package ru.netology.nmedia.repository


import kotlinx.coroutines.flow.Flow
import ru.netology.nmedia.dto.Post

interface PostRepository {

    suspend fun showAll()

    val dataRepo: Flow<List<Post>>
    fun getNewerCount(id: Long): Flow<Int>
    suspend fun getAll()

    suspend fun save(post: Post)
    suspend fun likeById(id: Long)
    suspend fun removeById(id: Long)


}



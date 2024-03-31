package ru.netology.nmedia.repository


import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import ru.netology.nmedia.auth.AuthState
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.model.PhotoModel


interface PostRepository {


    suspend fun refreshHide()

    suspend fun showAll()

    val dataRepo: Flow<PagingData<Post>>


    fun getNewerCount(id: Long): Flow<Int>
    suspend fun getAll()
    suspend fun authenticate(login: String, password: String) : AuthState
    suspend fun registerUser(login: String, password: String, name : String) : AuthState


    suspend fun save(post: Post, photo: PhotoModel?)
    suspend fun likeById(id: Long)
    suspend fun removeById(id: Long)


}



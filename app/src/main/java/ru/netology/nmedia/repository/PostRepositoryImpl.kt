package ru.netology.nmedia.repository


import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import ru.netology.nmedia.api.PostApi
import ru.netology.nmedia.dao.PostDao
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.entity.PostEntity
import ru.netology.nmedia.entity.PostEntityLocal
import ru.netology.nmedia.entity.toDto
import ru.netology.nmedia.entity.toEntity
import ru.netology.nmedia.error.ApiError
import ru.netology.nmedia.error.AppError
import ru.netology.nmedia.error.NetworkError
import ru.netology.nmedia.error.UnknownError
import java.io.IOException


class PostRepositoryImpl(private val dao: PostDao) : PostRepository {

    override val dataRepo = dao.getAll().map { it.toDto() } //.flowOn(Dispatchers.Default)
    override suspend fun getAll() {
        try {
            val response = PostApi.retrofitService.getAll()
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            val body = response.body() ?: throw ApiError(response.code(), response.message())
            dao.insert2(body.toEntity())

        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        } catch (e: ApiError) {
            throw e
        }
    }

    override fun getNewerCount(id: Long): Flow<Int> = flow {

        while (true) {
            delay(10_000L)
            val response = PostApi.retrofitService.getNewerCount(id)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            val body = response.body() ?: throw ApiError(response.code(), response.message())
            val copyBody = body.map { it.copy(hide = true) }
            dao.insert2(copyBody.toEntity())
            emit(dao.countHidden())

        }
    }.catch { e -> throw AppError.from(e) }
        .flowOn(Dispatchers.Default)

    override suspend fun likeById(id: Long) {
        try {
            dao.likeById(id)
            val response = PostApi.retrofitService.likeById(id)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }

            val body = response.body() ?: throw ApiError(response.code(), response.message())
            val post = PostEntity.fromDto(body)
            dao.insert(post)
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        } catch (e: ApiError) {
            throw e
        }

    }

    override suspend fun save(post: Post) {
        try {
            dao.insertLocal(PostEntityLocal.fromDto(post))

            val response = PostApi.retrofitService.save(post)
            if (!response.isSuccessful) throw ApiError(response.code(), response.message())
            val body = response.body() ?: throw ApiError(response.code(), response.message())

            dao.removeByIdLocal(post.id)
            val postId = PostEntity.fromDto(body)
            dao.insert(postId)
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        } catch (e: ApiError) {
            throw e
        }
    }

    override suspend fun showAll (){
        dao.showAll()
    }


    override suspend fun removeById(id: Long) {
        try {
            dao.removeById(id)
            val response = PostApi.retrofitService.removeById(id)
            if (!response.isSuccessful) throw ApiError(response.code(), response.message())
            dao.removeById(id)
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        } catch (e: ApiError) {
            throw e
        }
    }


}



package ru.netology.nmedia.repository


import androidx.lifecycle.map
import ru.netology.nmedia.api.PostApi
import ru.netology.nmedia.dao.PostDao
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.entity.PostEntity
import ru.netology.nmedia.entity.toDto
import ru.netology.nmedia.entity.toEntity
import ru.netology.nmedia.error.ApiException
import ru.netology.nmedia.error.NetworkException
import ru.netology.nmedia.error.UnknownException
import java.io.IOException


class PostRepositoryImpl(private val dao: PostDao) : PostRepository {

    override val data = dao.getAll().map { it.toDto() }
    override suspend fun getAll() {
        try {
            val response = PostApi.retrofitService.getAll()
            if (!response.isSuccessful) {
                throw ApiException(response.code(), response.message())
            }
            val body = response.body() ?: throw ApiException(response.code(), response.message())
            dao.insert2(body.toEntity())

        } catch (e: IOException) {
            throw NetworkException
        } catch (e: Exception) {
            throw UnknownException
        } catch (e: ApiException) {
            throw e
        }
    }

        override suspend fun likeById(id: Long)  {
            try {
                val response =  PostApi.retrofitService.likeById(id)
                if (!response.isSuccessful){
                    throw ApiException(response.code(), response.message())
                }

                val body = response.body() ?: throw ApiException(response.code(), response.message())
                val post = PostEntity.fromDto(body)
                dao.insert(post)
            } catch (e: IOException) {
                throw NetworkException
            } catch (e: Exception) {
                throw UnknownException
            } catch (e: ApiException) {
                throw e
            }

        }

        override suspend fun save(post: Post) {
            try {
                val response =   PostApi.retrofitService.save(post)
                if (!response.isSuccessful) throw ApiException (response.code(), response.message())

                val body = response.body() ?: throw ApiException(response.code(), response.message())
                val post = PostEntity.fromDto(body)
                dao.insert(post)
            } catch (e: IOException) {
                throw NetworkException
            } catch (e: Exception) {
                throw UnknownException
            } catch (e: ApiException) {
                throw e
            }
        }


        override suspend fun removeById(id: Long) {
            try {
                val response =   PostApi.retrofitService.removeById(id)
                if (!response.isSuccessful) throw ApiException (response.code(), response.message())
                dao.removeById(id)
            } catch (e: IOException) {
                throw NetworkException
            } catch (e: Exception) {
                throw UnknownException
            } catch (e: ApiException) {
                throw e
            }
        }


    }



package ru.netology.nmedia.repository


import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import ru.netology.nmedia.api.PostApi
import ru.netology.nmedia.auth.AuthState
import ru.netology.nmedia.dao.PostDao
import ru.netology.nmedia.dto.Attachment
import ru.netology.nmedia.dto.AttachmentType
import ru.netology.nmedia.dto.Media
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.entity.PostEntity
import ru.netology.nmedia.entity.toDto
import ru.netology.nmedia.entity.toEntity
import ru.netology.nmedia.error.ApiError
import ru.netology.nmedia.error.AppError
import ru.netology.nmedia.error.NetworkError
import ru.netology.nmedia.error.UnknownError
import ru.netology.nmedia.model.PhotoModel
import java.io.File
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

    override suspend fun authenticate(login: String, password: String) : AuthState {
        try {
            val response = PostApi.retrofitService.authenticate(login,password)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            val body: AuthState = response.body() ?: throw ApiError(response.code(), response.message())

            return AuthState(body.id, body.token)

        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        } catch (e: ApiError) {
            throw e
        }

    }

    override suspend fun registerUser(login: String, password: String, name: String): AuthState {
        try {
            val response = PostApi.retrofitService.registerUser(login,password,name)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            val body: AuthState = response.body() ?: throw ApiError(response.code(), response.message())

            return AuthState(body.id, body.token)

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

    override suspend fun save(post: Post, photo: PhotoModel?) {
        try {

           val postWithAttachment =  if (photo != null) {
                val media = upload(photo.file)
                post.copy(attachment = Attachment(media.id, AttachmentType.IMAGE))
            } else post
            dao.insert(PostEntity.fromDto(post))

            val response = PostApi.retrofitService.save(postWithAttachment)
            if (!response.isSuccessful) throw ApiError(response.code(), response.message())
            val body = response.body() ?: throw ApiError(response.code(), response.message())

            dao.removeByIdLocal(post.authorId)
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

    private suspend fun upload(file: File): Media {
        return PostApi.retrofitService.upload(
            MultipartBody.Part.createFormData(
                "file",
                file.name,
                file.asRequestBody()
            )

        )
    }

    override suspend fun showAll() {
        dao.showAll()
    }

    override suspend fun refreshHide() {
        dao.refreshHide(state = true, oppositeState = false)
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



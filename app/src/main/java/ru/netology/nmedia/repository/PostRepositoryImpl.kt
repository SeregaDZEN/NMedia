package ru.netology.nmedia.repository


import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.TerminalSeparatorType
import androidx.paging.insertSeparators
import androidx.paging.map
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import ru.netology.nmedia.R
import ru.netology.nmedia.api.ApiService
import ru.netology.nmedia.auth.AuthState
import ru.netology.nmedia.dao.PostDao
import ru.netology.nmedia.dao.PostRemoteKeyDao
import ru.netology.nmedia.db.AppDb
import ru.netology.nmedia.dto.Ad
import ru.netology.nmedia.dto.Attachment
import ru.netology.nmedia.dto.AttachmentType
import ru.netology.nmedia.dto.FeedItem
import ru.netology.nmedia.dto.Media
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.dto.TimeCheck
import ru.netology.nmedia.entity.PostEntity
import ru.netology.nmedia.error.ApiError
import ru.netology.nmedia.error.NetworkError
import ru.netology.nmedia.error.UnknownError
import ru.netology.nmedia.model.PhotoModel
import ru.netology.nmedia.util.ResourceService
import java.io.File
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random


@Singleton
class PostRepositoryImpl @Inject constructor(
    private val apiService: ApiService,
    private val postDao: PostDao,
    db: AppDb,
    postRemoteKeyDao: PostRemoteKeyDao,
    private val resourceService: ResourceService
) : PostRepository {

    @OptIn(ExperimentalPagingApi::class)
    override val dataRepo: Flow<PagingData<FeedItem>> = Pager(
        config = PagingConfig(10, enablePlaceholders = true),
        pagingSourceFactory = { postDao.getPagingSource() },
        remoteMediator = PostRemoteMediator(
            apiService, db, postDao, postRemoteKeyDao

        )
    ).flow
        .map { pagingData ->
            pagingData.map(PostEntity::toDto)
                .insertSeparators(TerminalSeparatorType.SOURCE_COMPLETE) { before, after ->
                    when {
                        before == null && after is Post -> {
                            // Для первого элемента в списке выведем разделитель согласно тому когда он написан
                            TimeCheck(
                                id = Random.nextLong(),
                                timestamp = after.published.timestamp()
                            )
                        }

                        before is Post && after is Post -> {
                            // Для элементов в середине списка вставляем разделитель, если временные рамки изменились
                            if (before.published.timestamp() != after.published.timestamp()) {
                                TimeCheck(
                                    id = Random.nextLong(),
                                    timestamp = after.published.timestamp()
                                )
                            } else null
                        }

                        else -> null
                    }
                } .insertSeparators(TerminalSeparatorType.FULLY_COMPLETE) { previous, _ ->
                    if (previous?.id?.rem(other = 7) == 0L) {
                        Ad(Random.nextLong(),  "figma.jpg")
                    } else null
                }
        }

    private fun Long.timestamp(): String {
        val oneDayInSec = 24 * 60 * 60
        val nowDay = (System.currentTimeMillis() / 1000) / oneDayInSec
        val inputDay = this / oneDayInSec
        return when (nowDay - inputDay) {
            0L -> resourceService.getString(R.string.today)
            1L -> resourceService.getString(R.string.yesterday)
            else -> resourceService.getString(R.string.last_week)
        }
    }


    override suspend fun authenticate(login: String, password: String): AuthState {
        try {
            val response = apiService.authenticate(login, password)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            val body: AuthState =
                response.body() ?: throw ApiError(response.code(), response.message())

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
            val response = apiService.registerUser(login, password, name)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            val body: AuthState =
                response.body() ?: throw ApiError(response.code(), response.message())

            return AuthState(body.id, body.token)

        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        } catch (e: ApiError) {
            throw e
        }
    }


    override fun getNewerCount(id: Long): Flow<Long> = flow {
        while (true) {
            delay(10_000L)
            emit(((apiService.getNewerCount(id).body()?.count ?: 0L)))
        }
    }

    override suspend fun likeById(id: Long) {
        try {
            postDao.likeById(id)
            val response = apiService.likeById(id)

            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }

            val body = response.body() ?: throw ApiError(response.code(), response.message())
            val post = PostEntity.fromDto(body)
            postDao.insert(post)
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

            val postWithAttachment = if (photo != null) {
                val media = upload(photo.file)
                post.copy(attachment = Attachment(media.id, AttachmentType.IMAGE))
            } else post
            postDao.insert(PostEntity.fromDto(post))

            val response = apiService.save(postWithAttachment)
            if (!response.isSuccessful) throw ApiError(response.code(), response.message())
            val body = response.body() ?: throw ApiError(response.code(), response.message())

            postDao.removeByIdLocal(post.authorId)
            val postId = PostEntity.fromDto(body)
            postDao.insert(postId)
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        } catch (e: ApiError) {
            throw e
        }
    }

    private suspend fun upload(file: File): Media {
        return apiService.upload(
            MultipartBody.Part.createFormData(
                "file",
                file.name,
                file.asRequestBody()
            )
        )
    }

    override suspend fun showAll() {
        postDao.showAll()
    }

    override suspend fun refreshHide() {
        postDao.refreshHide(state = true, oppositeState = false)
    }

    override suspend fun removeById(id: Long) {
        try {
            postDao.removeById(id)
            val response = apiService.removeById(id)
            if (!response.isSuccessful) throw ApiError(response.code(), response.message())
            postDao.removeById(id)
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        } catch (e: ApiError) {
            throw e
        }
    }
}
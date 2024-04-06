package ru.netology.nmedia.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import ru.netology.nmedia.api.ApiService
import ru.netology.nmedia.dao.PostDao
import ru.netology.nmedia.db.AppDb
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.entity.PostEntity
import ru.netology.nmedia.error.ApiError

@OptIn(ExperimentalPagingApi::class)
class PostRemoteMediator(
    private val apiService: ApiService,
   // private val db: AppDb,
    private val postDao: PostDao,
  //  private val postRemoteKeyDao: PostRemoteKeyDao,
) : RemoteMediator<Int, PostEntity>() {



    override suspend fun load(loadType: LoadType, state: PagingState<Int, PostEntity>): MediatorResult {
        try {
            val result = when (loadType) {

                LoadType.REFRESH -> apiService.getLatest(state.config.pageSize)

                LoadType.PREPEND -> {
                    val id = state.firstItemOrNull()?.id ?: return MediatorResult.Success(false)
                    apiService.getAfter(id, state.config.pageSize)
                }

                LoadType.APPEND -> {
                    val id = state.lastItemOrNull()?.id ?: return MediatorResult.Success(false)
                    apiService.getBefore(id, state.config.pageSize)
                }

            }
            if (!result.isSuccessful) {
                throw ApiError(result.code(),result.message() )
            }
            val body = result.body() ?: throw ApiError(result.code(),result.message() )



            postDao.insertList(body.map(PostEntity::fromDto))

            return MediatorResult.Success(body.isEmpty())

        } catch (e: Exception) {
            return MediatorResult.Error(e)
        }
    }
}
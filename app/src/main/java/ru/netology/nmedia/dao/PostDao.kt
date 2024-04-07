package ru.netology.nmedia.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import ru.netology.nmedia.entity.PostEntity
import ru.netology.nmedia.entity.PostEntityLocal

@Dao
interface PostDao {
    @Query("SELECT * FROM PostEntity ORDER BY id DESC")
    fun getAll(): Flow<List<PostEntity>>

    @Query("SELECT * FROM PostEntity ORDER BY id DESC")
    fun getPagingSource(): PagingSource<Int,PostEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(post: PostEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)

    suspend fun insertList(posts: List<PostEntity>)

    @Query("SELECT COUNT(*) FROM PostEntity WHERE hide = 1")
    suspend fun count(): Int

    @Query("UPDATE PostEntity SET hide = :state WHERE hide = :oppositeState")
    suspend fun refreshHide (state: Boolean, oppositeState: Boolean)


    @Query("UPDATE PostEntity SET hide = 0")
    suspend fun showAll()


    @Query(
        """
           UPDATE PostEntity SET
               likes = likes + CASE WHEN likedByMe THEN -1 ELSE 1 END,
               likedByMe = CASE WHEN likedByMe THEN 0 ELSE 1 END
           WHERE id = :id;
           """
    )
    suspend fun likeById(id: Long)




    @Query("DELETE FROM PostEntity WHERE id = :id")
    suspend fun removeById(id: Long)

    @Query("DELETE FROM PostEntity ")
    suspend fun clear()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLocal(post: PostEntityLocal)

    @Query("DELETE FROM PostEntityLocal WHERE id = :id")
    suspend fun removeByIdLocal(id: Long)
}

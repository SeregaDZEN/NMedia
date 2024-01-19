package ru.netology.nmedia.repository


import ru.netology.nmedia.dto.Post

interface PostRepository {
    fun getAllSync(callBack: RepositoryCallBack<List<Post>>)
    fun likeById(id: Long, likeOr: Boolean, callBack: RepositoryCallBack<Post>)
    fun save(post: Post, callBack: SaveCallBack<Post>)
    fun removeById(id: Long, callBack: RepositoryCallBack<Unit>)

    interface SaveCallBack<T> {
        fun <U> onSuccess(result: U)
        fun onError(e: Throwable)
    }

    interface RepositoryCallBack<T> {
        fun onSuccess(result: T)
        fun onError(e: Throwable)
    }

}



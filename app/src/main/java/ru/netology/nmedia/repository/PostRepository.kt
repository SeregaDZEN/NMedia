package ru.netology.nmedia.repository


import ru.netology.nmedia.dto.Post

interface PostRepository {
    fun getAllSync(callBack: RepositoryCallBack<List<Post>>)
    fun likeById(id: Long, likeOr: Boolean, callBack: RepositoryCallBack<Post>)
    fun save(post: Post, callBack: RepositoryCallBack<Post>)
    fun removeById(id: Long, callBack: RepositoryCallBack<Unit>)

    interface RepositoryCallBack<T> {
        fun onSuccess(result: T)
        fun onError(e: Throwable)
    }

}



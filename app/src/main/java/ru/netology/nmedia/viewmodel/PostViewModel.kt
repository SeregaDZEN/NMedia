package ru.netology.nmedia.viewmodel

import android.app.Application
import androidx.lifecycle.*
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.model.FeedModel
import ru.netology.nmedia.repository.*
import ru.netology.nmedia.util.SingleLiveEvent

private val empty = Post(
    id = 0,
    content = "",
    author = "",
    likedByMe = false,
    likes = 0,
    published = "",
    authorAvatar = ""

)

class PostViewModel(application: Application) : AndroidViewModel(application) {
    // упрощённый вариант
    private val repository: PostRepository = PostRepositoryImpl()
    private val _data = MutableLiveData(FeedModel())
    val data: LiveData<FeedModel>
        get() = _data
    private val edited = MutableLiveData(empty)
    private val _postCreated = SingleLiveEvent<Unit>()
    val postCreated: LiveData<Unit>
        get() = _postCreated

    init {
        loadPosts()
    }

    fun removeById(id: Long) {

        val old = _data.value?.posts.orEmpty()

        _data.postValue(
            _data.value?.copy(posts = _data.value?.posts.orEmpty()
                .filter { it.id != id }
            )
        )

        val postDelRepo = object : PostRepository.RepositoryCallBack<Unit> {
            override fun onSuccess(result: Unit) {
            }

            override fun onError(e: Throwable) {
                _data.postValue(_data.value?.copy(posts = old, error = true))
            }
        }

        repository.removeById(id, postDelRepo)
    }

    fun loadPosts() {


        val dataRepo = object : PostRepository.RepositoryCallBack<List<Post>> {
            override fun onError(throwable: Throwable) {
                _data.postValue(FeedModel(error = true))
            }

            override fun onSuccess(posts: List<Post>) {
                _data.postValue((FeedModel(posts = posts, empty = posts.isEmpty())))
            }
        }
        repository.getAllSync(dataRepo)
    }


    fun save() {

        val oldPosts = _data.value?.posts.orEmpty()
        val dataRepoSave = object : PostRepository.RepositoryCallBack<Post> {
            override fun onSuccess(result: Post) {
            }

            override fun onError(throwable: Throwable) {
                _data.postValue(_data.value?.copy(posts = oldPosts, error = true))
            }

        }

        edited.value?.let {

            repository.save(it, dataRepoSave)
            _postCreated.postValue(Unit)

        }
        edited.value = empty
    }

    fun edit(post: Post) {
        edited.value = post
    }

    fun changeContent(content: String) {
        val text = content.trim()
        if (edited.value?.content == text) {
            return
        }
        edited.value = edited.value?.copy(content = text)
    }

    fun like(post: Post) {

        val old = _data.value?.posts.orEmpty()

        repository.likeById(post.id, post.likedByMe,
            object : PostRepository.RepositoryCallBack<Post> {
                override fun onSuccess(result: Post) {
                    _data.postValue(
                        _data.value?.copy(posts = _data.value?.posts.orEmpty()
                            .map { if (it.id == result.id) result else it }
                        ))
                }

                override fun onError(e: Throwable) {
                    _data.postValue(_data.value?.copy(posts = old, error = true))
                }

            }
        )
    }


}

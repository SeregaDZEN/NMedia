package ru.netology.nmedia.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import ru.netology.nmedia.auth.AppAuth
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.model.FeedModel
import ru.netology.nmedia.model.FeedModelState
import ru.netology.nmedia.model.PhotoModel
import ru.netology.nmedia.repository.PostRepository
import ru.netology.nmedia.util.SingleLiveEvent
import javax.inject.Inject

private val empty = Post(
    id = 0,
    authorId = 0,
    content = "",
    author = "",
    authorAvatar = "",
    published = 0,
    likedByMe = false,
    hide = false,
    likes = 0,
    attachment = null,
    ownedByMe = false


)
@HiltViewModel
class PostViewModel @Inject constructor(
    private val repository: PostRepository,
    appAuth: AppAuth,

    ) : ViewModel() {

    private val _dataState = MutableLiveData(FeedModelState())

    @OptIn(ExperimentalCoroutinesApi::class)
    val data: LiveData<FeedModel> = appAuth
        .authState.flatMapLatest { auth ->
            repository.dataRepo.map { posts ->

                FeedModel(
                    posts.map { it.copy(ownedByMe = auth.id == it.authorId) }.filter { !it.hide },
                    posts.isEmpty()
                )


            }
        }.asLiveData(Dispatchers.Default)


    // А здесь берём все посты в т.ч. скрытые для запроса новых с сервера
    val newerCount = repository.dataRepo.asLiveData()
        .switchMap {
            repository.getNewerCount(it.firstOrNull()?.authorId ?: 0L).catch {
                _dataState.postValue(
                    FeedModelState(error = true)
                )
            }.asLiveData(Dispatchers.Default, 100)
        }


    private val _photo = MutableLiveData<PhotoModel?>(null)
    val photo: LiveData<PhotoModel?>
        get() = _photo
    val dataState: LiveData<FeedModelState>
        get() = _dataState
    private val edited: MutableLiveData<Post> = MutableLiveData(empty)
    private val _postCreated = SingleLiveEvent<Unit>()
    val postCreated: LiveData<Unit>
        get() = _postCreated

    init {
        loadPosts()

    }

    fun savePhoto(photoModel: PhotoModel) {
        _photo.value = photoModel
    }

    fun clear() {
        _photo.value = null
    }


    fun showAll() {
        viewModelScope.launch { repository.showAll() }
    }

    fun refreshHide() {
        viewModelScope.launch { repository.refreshHide() }
    }


    fun removeById(id: Long) {
        viewModelScope.launch {
            repository.removeById(id)
        }
    }

    fun loadPosts() = viewModelScope.launch {
        try {
            _dataState.value = FeedModelState(loading = true)
            repository.getAll()
            _dataState.value = FeedModelState()
        } catch (e: Exception) {
            _dataState.value = FeedModelState(error = true)
        }
    }


    fun save() {

        viewModelScope.launch {
            try {
                edited.value?.let { repository.save(it, _photo.value) }
            } catch (e: Exception) {
                edited.value = empty
            }
        }
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
        viewModelScope.launch {
            try {
                repository.likeById(post.authorId)
            } catch (e: Exception) {
                _dataState.value = FeedModelState(error = true)
            }
        }
    }

}




package ru.netology.nmedia.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import ru.netology.nmedia.db.AppDb
import ru.netology.nmedia.dto.Media
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.model.FeedModel
import ru.netology.nmedia.model.FeedModelState
import ru.netology.nmedia.model.PhotoModel
import ru.netology.nmedia.repository.PostRepository
import ru.netology.nmedia.repository.PostRepositoryImpl
import ru.netology.nmedia.util.SingleLiveEvent

private val empty = Post(
    id = 0,
    content = "",
    author = "",
    authorAvatar = "",
    published = 0,
    likedByMe = false,
    hide = false,
    likes = 0,
    attachment = null

)

class PostViewModel(application: Application) : AndroidViewModel(application) {
    // упрощённый вариант
    private val repository: PostRepository =
        PostRepositoryImpl(AppDb.getInstance(application).postDao())
    private val _dataState = MutableLiveData(FeedModelState())

    val data: LiveData<FeedModel> = repository.dataRepo.map { posts ->
        // Фильтруем только видимые
        posts.filter { !it.hide }
    }.map(::FeedModel).catch { it.printStackTrace() }.asLiveData(Dispatchers.Default)


    // А здесь берём все посты в т.ч. скрытые для запроса новых с сервера
    val newerCount = repository.dataRepo.asLiveData()
        .switchMap {
            repository.getNewerCount(it.firstOrNull()?.id ?: 0L).catch {
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
    fun clear (){
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
            edited.value?.let { repository.save(it, _photo.value) }
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


        viewModelScope.launch {

            try {
                repository.likeById(post.id)
            } catch (e: Exception) {
                _dataState.value = FeedModelState(error = true)
            }
        }
    }

}




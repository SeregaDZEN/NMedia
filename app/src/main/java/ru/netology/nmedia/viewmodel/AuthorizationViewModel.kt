package ru.netology.nmedia.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ru.netology.nmedia.auth.AppAuth
import ru.netology.nmedia.db.AppDb
import ru.netology.nmedia.error.NetworkError
import ru.netology.nmedia.repository.PostRepository
import ru.netology.nmedia.repository.PostRepositoryImpl

class AuthorizationViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: PostRepository =
        PostRepositoryImpl(AppDb.getInstance(application).postDao())


    val _authState = AppAuth.getInstance().authState
    //   val authState: LiveData<AuthState> = _authState

    fun authenticate(login: String, password: String) {
        viewModelScope.launch {
            try {
                val result = repository.authenticate(login, password)

                AppAuth.getInstance().setFlow(result)
            } catch (e: NetworkError) {

                AppAuth.getInstance().setFlow(_authState.value.copy(error = e))

            }

        }
    }
}



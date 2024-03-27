package ru.netology.nmedia.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import ru.netology.nmedia.auth.AppAuth
import ru.netology.nmedia.error.NetworkError
import ru.netology.nmedia.repository.PostRepository
import javax.inject.Inject

@HiltViewModel
class AuthorizationViewModel @Inject constructor(
    private val repository: PostRepository,
    private val appAuth: AppAuth,
) : ViewModel() {


    val _authState = appAuth.authState
    //   val authState: LiveData<AuthState> = _authState

    fun authenticate(login: String, password: String) {
        viewModelScope.launch {
            try {
                val result = repository.authenticate(login, password)

                appAuth.setFlow(result)
            } catch (e: NetworkError) {

                appAuth.setFlow(_authState.value.copy(error = e))

            }

        }
    }
}



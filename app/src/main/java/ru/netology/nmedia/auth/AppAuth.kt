package ru.netology.nmedia.auth

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import ru.netology.nmedia.db.AppDb

class AppAuth private constructor(context: Context) {

    private val prefs = context.getSharedPreferences("auth", Context.MODE_PRIVATE)

    private val _authState = MutableStateFlow(
        AuthState(prefs.getLong(KEY_ID, 0L), prefs.getString(KEY_TOKEN, null))
    )

    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    fun setFlow(authState:AuthState){
        _authState.value = authState
        with(prefs.edit()) {
            putLong(KEY_ID, authState.id)
            putString(KEY_TOKEN, authState.token)
            commit()
        }
    }
    @Synchronized
    fun setAuth(id: Long, token: String) {
        _authState.value = AuthState(id, token)
        with(prefs.edit()) {
            putLong(KEY_ID, id)
            putString(KEY_TOKEN, token)
            commit()
        }

    }

    @Synchronized
    fun removeAuth() {
        _authState.value = AuthState()
        with(prefs.edit()) {
            clear()
            commit()
        }
    }

        companion object {
            private const val KEY_ID = "id"
            private const val KEY_TOKEN = "token"

            @Volatile
            private var instance: AppAuth? = null

            fun getInstance() = synchronized(this) {
                instance ?: throw IllegalAccessError("getInstance should be called only after initAuth")
            }

            fun initAuth(context: Context) = instance ?: synchronized(this) {
                instance ?: AppAuth(context).also { instance = it }
            }
        }
    }

    data class AuthState(val id: Long = 0L, val token: String? = null, val name: String? = null, val error: Exception? = null)
package ru.netology.nmedia.auth

import android.content.Context
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import ru.netology.nmedia.api.ApiService
import ru.netology.nmedia.dto.PushToken
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppAuth @Inject constructor(
    @ApplicationContext
    private val context: Context
) {
    private val idKey = "id"
    private val tokenKey = "token"

    init {
        sendPushToken()
    }

    private val prefs = context.getSharedPreferences("auth", Context.MODE_PRIVATE)

    private val _authState = MutableStateFlow(
        AuthState(prefs.getLong(idKey, 0L), prefs.getString(tokenKey, null))
    )

    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    fun setFlow(authState: AuthState) {
        _authState.value = authState
        with(prefs.edit()) {
            putLong(idKey, authState.id)
            putString(tokenKey, authState.token)
            commit()
        }
        sendPushToken()
    }


    @Synchronized
    fun removeAuth() {
        _authState.value = AuthState()
        with(prefs.edit()) {
            clear()
            commit()
        }
        sendPushToken()
    }

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface AppAuthEntryPoint {
        fun getApiService(): ApiService
    }

    fun sendPushToken(token: String? = null) {
        CoroutineScope(Dispatchers.Default).launch {
            try {
                val tokenDto = PushToken(token ?: FirebaseMessaging.getInstance().token.await())
                val entryPoint =
                    EntryPointAccessors.fromApplication(context, AppAuthEntryPoint::class.java)
                entryPoint.getApiService().sendPushToken(tokenDto)
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }
    }

}

data class AuthState(
    val id: Long = 0L,
    val token: String? = null,
    val name: String? = null,
    val error: Exception? = null
)
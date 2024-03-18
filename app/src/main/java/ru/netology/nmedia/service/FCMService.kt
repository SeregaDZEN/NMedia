package ru.netology.nmedia.service

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import ru.netology.nmedia.R
import ru.netology.nmedia.api.PostApi
import ru.netology.nmedia.auth.AppAuth
import ru.netology.nmedia.dto.PushContent
import ru.netology.nmedia.dto.PushToken
import kotlin.random.Random


class FCMService : FirebaseMessagingService() {
    private val action = "action"
    private val content = "content"
    private val channelId = "remote"
    private val gson = Gson()


    override fun onCreate() {
        super.onCreate()
        val name = getString(R.string.channel_remote_name)
        val descriptionText = getString(R.string.channel_remote_description)
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(channelId, name, importance).apply {
            description = descriptionText
        }
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)
    }

    private fun createNotification(context: Context, title: String, content: String): Notification {
        return NotificationCompat.Builder(context, content)
            .setContentTitle(title)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
    }

    override fun onMessageReceived(message: RemoteMessage) {

        val valueIdLocal = AppAuth.getInstance().authState.value.id
        val valueIdServer = Gson().fromJson(message.data["content"], PushContent::class.java)

        if (valueIdLocal == valueIdServer.recipientId || valueIdServer.recipientId == null) {
            val notification = createNotification(
                this,
                "оповещение что всё норм или не особо",
                content
            )
            notify(notification)
        }

        if (valueIdServer.recipientId == 0L || valueIdServer.recipientId != valueIdLocal) {
            val notification = createNotification(
                this,
                "сервер считает, что у вас анонимная аутентификация и вам нужно переотправить свой push token",
                content
            )
            notify(notification)
            AppAuth.getInstance().sendPushToken()
        }else {
            val notification = createNotification(
                this,
                "на вашем устройстве другая аутентификация",
                content
            )
            notify(notification)
            AppAuth.getInstance().sendPushToken()
        }

    }

    override fun onNewToken(token: String) {
        AppAuth.getInstance().sendPushToken(token)
    }

    private fun handleLike(content: Like) {
        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(
                getString(
                    R.string.notification_user_liked,
                    content.userName,
                    content.postAuthor,
                )
            )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        notify(notification)
    }

    private fun notify(notification: Notification) {
        if (
            Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            checkSelfPermission(
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            NotificationManagerCompat.from(this)
                .notify(Random.nextInt(100_000), notification)
        }
    }
}

enum class Action {
    LIKE,
}

data class Like(
    val userId: Long,
    val userName: String,
    val postId: Long,
    val postAuthor: String,
)


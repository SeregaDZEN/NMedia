package ru.netology.nmedia.repository

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.internal.EMPTY_REQUEST
import ru.netology.nmedia.dto.Post
import java.io.IOException
import java.util.concurrent.TimeUnit


class PostRepositoryImpl : PostRepository {
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .build()
    private val gson = Gson()
    private val typeToken = object : TypeToken<List<Post>>() {}

    companion object {
        private const val BASE_URL = "http://10.0.2.2:9999"
        private val jsonType = "application/json".toMediaType()
    }


    override fun getAllSync(callBack: PostRepository.RepositoryCallBack<List<Post>>) {
        val request: Request = Request.Builder()
            .url("${BASE_URL}/api/slow/posts")
            .build()

        client.newCall(request)
            .enqueue(
                object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        callBack.onError(e)
                    }

                    override fun onResponse(call: Call, response: Response) {
                        val responseText = response.body?.string()
                        if (responseText == null) {
                            callBack.onError(RuntimeException("body is null"))
                            return
                        }
                        try {
                            callBack.onSuccess(gson.fromJson(responseText, typeToken))
                        } catch (e: Exception) {
                            callBack.onError(e)
                        }
                    }
                }
            )

    }

    override fun likeById(
        id: Long,
        likeOr: Boolean,
        callBack: PostRepository.RepositoryCallBack<Post>
    ) {
        val request = if (likeOr) {
            Request.Builder()
                .delete(EMPTY_REQUEST)
                .url("${BASE_URL}/api/slow/posts/${id}/likes")
                .build()
        } else {
            Request.Builder()
                .post(EMPTY_REQUEST)
                .url("${BASE_URL}/api/slow/posts/${id}/likes")
                .build()
        }


        client.newCall(request).enqueue(
            object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    callBack.onError(e)
                }

                override fun onResponse(call: Call, response: Response) {
                    val responseText = response.body?.string()
                    if (responseText == null) {
                        callBack.onError(RuntimeException("body is null"))
                        return
                    }
                    try {
                        callBack.onSuccess(gson.fromJson(responseText, Post::class.java))
                    } catch (e: Exception) {
                        callBack.onError(e)
                    }
                }
            }
        )


    }

    override fun save(post: Post, callBack: PostRepository.RepositoryCallBack<Post>) {
        val request: Request = Request.Builder()
            .post(gson.toJson(post).toRequestBody(jsonType))
            .url("${BASE_URL}/api/slow/posts")
            .build()

        client.newCall(request)
            .enqueue(
                object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        callBack.onError(e)
                    }

                    override fun onResponse(call: Call, response: Response) {
                        val responseText = response.body?.string()
                        if (responseText == null) {
                            callBack.onError(RuntimeException("body is null"))
                            return
                        }
                        try {
                            callBack.onSuccess(gson.fromJson(responseText, Post::class.java))
                        } catch (e: Exception) {
                            callBack.onError(e)
                        }
                    }
                }
            )
    }

    override fun removeById(id: Long, callBack: PostRepository.RepositoryCallBack<Unit>) {
        val request: Request = Request.Builder()
            .delete()
            .url("${BASE_URL}/api/slow/posts/$id")
            .build()

        client.newCall(request)
            .enqueue(
                object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        callBack.onError(e)
                    }

                    override fun onResponse(call: Call, response: Response) {
                        val success = response.isSuccessful
                        if (success) callBack.onSuccess(Unit) else callBack.onError(
                            RuntimeException(
                                "error"
                            )
                        )
                    }
                }
            )
//            .close()
    }
}

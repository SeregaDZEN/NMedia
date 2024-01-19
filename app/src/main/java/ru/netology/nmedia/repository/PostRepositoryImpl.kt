package ru.netology.nmedia.repository


import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import ru.netology.nmedia.api.PostApi
import ru.netology.nmedia.dto.Post


class PostRepositoryImpl : PostRepository {
//    private val client = OkHttpClient.Builder()
//        .connectTimeout(30, TimeUnit.SECONDS)
//        .build()
//    private val gson = Gson()
//    private val typeToken = object : TypeToken<List<Post>>() {}
//
//    companion object {
//        private const val BASE_URL = "http://10.0.2.2:9999"
//        private val jsonType = "application/json".toMediaType()
//    }


    override fun getAllSync(callBack: PostRepository.RepositoryCallBack<List<Post>>) {

        PostApi.retrofitService.getAll().enqueue(object : Callback<List<Post>> {
            override fun onResponse(call: Call<List<Post>>, response: Response<List<Post>>) {
                println(Thread.currentThread().name)

                if (response.isSuccessful) {
                    callBack.onSuccess(response.body() ?: throw RuntimeException("empty body"))
                } else {
                    callBack.onError(RuntimeException("error code: ${response.code()} with ${response.message()}"))
                }
            }

            override fun onFailure(call: Call<List<Post>>, t: Throwable) {
                callBack.onError(Exception(t))
            }
        })
    }


    override fun likeById(
        id: Long,
        likeOr: Boolean,
        callBack: PostRepository.RepositoryCallBack<Post>
    ) {
        val request = if (likeOr) {
            PostApi.retrofitService.dislikeById(id)
        } else {
            PostApi.retrofitService.likeById(id)
        }

        request
            .enqueue(
                object : Callback<Post> {
                    override fun onResponse(call: Call<Post>, response: Response<Post>) {
                        if (response.isSuccessful) {
                            callBack.onSuccess(
                                response.body() ?: throw RuntimeException("empty body")
                            )
                        } else {
                            callBack.onError(RuntimeException("error code: ${response.code()} with ${response.message()}"))
                        }
                    }

                    override fun onFailure(call: Call<Post>, t: Throwable) {
                        callBack.onError(Exception(t))
                    }
                }
            )

    }


    override fun save(post: Post, callBack: PostRepository.SaveCallBack<Post>) {
        PostApi.retrofitService.save(post)
            .enqueue(
                object : Callback<Post> {

                    override fun onResponse(call: Call<Post>, response: Response<Post>) {
                        if (response.isSuccessful) {
                            callBack.onSuccess(Unit)
                        } else {
                            callBack.onError(RuntimeException("error code: ${response.code()} with ${response.message()}"))
                        }
                    }

                    override fun onFailure(call: Call<Post>, t: Throwable) {
                        callBack.onError(Exception(t))
                    }


                }
            )
    }

    override fun removeById(id: Long, callBack: PostRepository.RepositoryCallBack<Unit>) {
        PostApi.retrofitService.removeById(id)
            .enqueue(
                object : Callback<Unit> {

                    override fun onFailure(call: Call<Unit>, t: Throwable) {
                        callBack.onError(Exception(t))
                    }

                    override fun onResponse(call: Call<Unit>, response: Response<Unit>) {
                        val success = response.isSuccessful
                        if (success) callBack.onSuccess(Unit) else callBack.onError(
                            RuntimeException(
                                "error"
                            )
                        )
                    }
                }
            )

    }
}



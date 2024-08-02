package com.mccarty.networkrequest.network

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonSyntaxException
import okhttp3.Request
import okio.Timeout
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException

class NetworkRequestCall <T>(private val delegate: Call<T>) : Call<NetworkRequest<T>> {
    override fun enqueue(callback: Callback<NetworkRequest<T>>) {
        return delegate.enqueue(object : Callback<T> {

            override fun onResponse(call: Call<T>, response: Response<T>) {
                val body = response.body()
                val code = response.code()
                val error = response.errorBody()
                if (response.isSuccessful) {
                    if (body != null) {
                        callback.onResponse(
                            this@NetworkRequestCall,
                            Response.success(NetworkRequest.Success(body))
                        )
                    } else {
                        callback.onResponse(
                            this@NetworkRequestCall,
                            Response.success(NetworkRequest.Error("No Response Body")),
                        )
                    }
                } else {
                    try {
                        val message = Gson().fromJson(error?.string(), JsonObject::class.java)
                            .get("message")?.asString
                            ?: "Response Error"
                        callback.onResponse(
                            this@NetworkRequestCall,
                            Response.success(NetworkRequest.Error("Response code error $message")),
                        )
                    } catch (jse: JsonSyntaxException) {
                        callback.onResponse(
                            this@NetworkRequestCall,
                            Response.success(NetworkRequest.Error("JSON Parse Error $code")),
                        )
                    }
                }
            }

            override fun onFailure(call: Call<T>, throwable: Throwable) {
                val response = when(throwable) {
                    is IOException -> {
                        NetworkRequest.Error<T>("Network error")
                    }
                    else -> {
                        NetworkRequest.Error("Network failure")
                    }
                }
                callback.onResponse(this@NetworkRequestCall, Response.success(response))
            }
        })
    }

    override fun clone(): Call<NetworkRequest<T>> = NetworkRequestCall(delegate.clone())

    override fun execute(): Response<NetworkRequest<T>> =
        throw UnsupportedOperationException("Does not support execute")

    override fun isExecuted(): Boolean = delegate.isExecuted

    override fun cancel() { delegate.cancel() }

    override fun isCanceled(): Boolean = delegate.isCanceled

    override fun request(): Request = delegate.request()

    override fun timeout(): Timeout = delegate.timeout()
}
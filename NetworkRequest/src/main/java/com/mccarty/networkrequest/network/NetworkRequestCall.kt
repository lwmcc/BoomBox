package com.mccarty.networkrequest.network

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import okhttp3.Request
import okio.Timeout
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException

class NetworkRequestCall <T : Any>(
    private val delegate: Call<T>
) : Call<NetworkRequest<T>> {
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
                    }
                } else {
                    callback.onResponse(
                        this@NetworkRequestCall,
                        Response.success(NetworkRequest.Error())
                    )
                }
            }

            override fun onFailure(call: Call<T>, throwable: Throwable) {
                val response = when(throwable) {
                    is IOException -> {
                        NetworkRequest.Error<T>()
                    } // TODO: change this
                    else -> {
                        NetworkRequest.Error<T>()
                    }
                }
                callback.onResponse(this@NetworkRequestCall, Response.success(response))
            }
        })
    }

    override fun clone(): Call<NetworkRequest<T>> = NetworkRequestCall(delegate.clone()) // TODO error?

    override fun execute(): Response<NetworkRequest<T>> = throw UnsupportedOperationException("Does not support execute")


    override fun isExecuted(): Boolean = delegate.isExecuted

    override fun cancel() { delegate.cancel() }

    override fun isCanceled(): Boolean = delegate.isCanceled

    override fun request(): Request = delegate.request()

    override fun timeout(): Timeout = delegate.timeout()

}
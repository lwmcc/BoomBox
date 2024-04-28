package com.mccarty.networkrequest.network

import retrofit2.Call
import retrofit2.CallAdapter
import retrofit2.Retrofit
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

class NetworkRequestAdapterFactory private constructor() : CallAdapter.Factory() {
    override fun get(
        type: Type,
        annotations: Array<out Annotation>,
        retrofit: Retrofit
    ): CallAdapter<*, *>? {
        if (getRawType(type) != Call::class.java) {
            return null
        }

        val callType = getParameterUpperBound(0, type as ParameterizedType)
        if (getRawType(callType) != NetworkRequest::class.java) {
            return null
        }

        val resultType = getParameterUpperBound(0, callType as ParameterizedType)
        return NetworkRequestAdapter(resultType)
    }

    companion object {
        fun create(): NetworkRequestAdapterFactory = NetworkRequestAdapterFactory()
    }
}
package com.mccarty.networkrequest.network

import retrofit2.Call
import retrofit2.CallAdapter
import java.lang.reflect.Type

class NetworkRequestAdapter(private val successType: Type) : CallAdapter<Type, Call<NetworkRequest<Type>>> {
    override fun responseType(): Type = successType
    override fun adapt(call: Call<Type>): Call<NetworkRequest<Type>> {
        return NetworkRequestCall(call)
    }
}

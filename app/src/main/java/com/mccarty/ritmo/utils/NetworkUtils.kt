package com.mccarty.ritmo.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import androidx.annotation.RequiresApi
import javax.inject.Inject

fun hasNetworkConnection(context: Context): Boolean {
    val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    connectivityManager.let { manager ->
        val capabilities =
            manager.getNetworkCapabilities(connectivityManager.activeNetwork)

        return capabilities?.let {
            it.hasCapability(NetworkCapabilities.NET_CAPABILITY_MMS) ||
                    it.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) ||
                    it.hasCapability(NetworkCapabilities.NET_CAPABILITY_FOTA)
        } ?: false
    }
}

class NetworkUtils @Inject constructor() {

    @Inject
    lateinit var networkCapabilities: NetworkCapabilities

    @RequiresApi(Build.VERSION_CODES.R)
    fun hasNetworkConnection(): Boolean {
        //networkCapabilities = NetworkCapabilities
        return networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
}
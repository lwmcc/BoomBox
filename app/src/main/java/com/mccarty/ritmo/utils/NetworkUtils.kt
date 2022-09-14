package com.mccarty.ritmo.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities

fun hasNetworkConnection(context: Context): Boolean {

    val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    connectivityManager.let {
        val capabilities =
            connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)

        return capabilities?.let {
            capabilities.hasCapability(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                    capabilities.hasCapability(NetworkCapabilities.TRANSPORT_WIFI) ||
                    capabilities.hasCapability(NetworkCapabilities.TRANSPORT_ETHERNET)
        } ?: false
    }

    return false
}
package com.mccarty.ritmo.utils

import android.content.Context
import android.media.ApplicationMediaCapabilities
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import androidx.test.core.app.ApplicationProvider
import io.mockk.*
import org.junit.Before
import org.junit.Test

class NetworkUtilsTest {

    private val mockContext = mockkClass(Context::class)
    private val mockConnectivityManager = mockkClass(ConnectivityManager::class)
    private val mockNetworkCapabilities = mockkClass(NetworkCapabilities::class)

    @Test
    fun `verify has network connection has capability transport cellular`() {
        // Arrange
        every { mockContext.getSystemService(Context.CONNECTIVITY_SERVICE) } returns mockConnectivityManager
        every { mockNetworkCapabilities.hasCapability(NetworkCapabilities.TRANSPORT_CELLULAR) } returns true
        every { mockConnectivityManager.getNetworkCapabilities(mockConnectivityManager.activeNetwork) } returns mockNetworkCapabilities

        // Act
        hasNetworkConnection(mockContext)

        // Assert
        verify { mockNetworkCapabilities.hasCapability(NetworkCapabilities.TRANSPORT_CELLULAR) }
    }
}
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
    fun `verify has network connection when capability is transport cellular`() {
        clearAllMocks()
        // Arrange
        every { mockContext.getSystemService(Context.CONNECTIVITY_SERVICE) } returns mockConnectivityManager
        every { mockNetworkCapabilities.hasCapability(NetworkCapabilities.TRANSPORT_CELLULAR) } returns true
        every { mockNetworkCapabilities.hasCapability(NetworkCapabilities.TRANSPORT_WIFI) } returns false
        every { mockNetworkCapabilities.hasCapability(NetworkCapabilities.TRANSPORT_ETHERNET) } returns false
        every { mockConnectivityManager.getNetworkCapabilities(mockConnectivityManager.activeNetwork) } returns mockNetworkCapabilities

        // Act
        hasNetworkConnection(mockContext)

        // Assert
        verify { mockNetworkCapabilities.hasCapability(NetworkCapabilities.TRANSPORT_CELLULAR) }
    }

/*    @Test
    fun `verify has network connection when capability is transport wifi`() {
        clearAllMocks()
        // Arrange
        every { mockContext.getSystemService(Context.CONNECTIVITY_SERVICE) } returns mockConnectivityManager
        every { mockNetworkCapabilities.hasCapability(NetworkCapabilities.TRANSPORT_CELLULAR) } returns false
        every { mockNetworkCapabilities.hasCapability(NetworkCapabilities.TRANSPORT_WIFI) } returns true
        every { mockNetworkCapabilities.hasCapability(NetworkCapabilities.TRANSPORT_ETHERNET) } returns false
        every { mockConnectivityManager.getNetworkCapabilities(mockConnectivityManager.activeNetwork) } returns mockNetworkCapabilities

        // Act
        hasNetworkConnection(mockContext)

        // Assert
        verify { mockNetworkCapabilities.hasCapability(NetworkCapabilities.TRANSPORT_WIFI) }
    }*/

/*    @Test
    fun `verify has network connection when capability is transport ethernet`() {
        clearAllMocks()
        // Arrange
        every { mockContext.getSystemService(Context.CONNECTIVITY_SERVICE) } returns mockConnectivityManager
        every { mockNetworkCapabilities.hasCapability(NetworkCapabilities.TRANSPORT_CELLULAR) } returns false
        every { mockNetworkCapabilities.hasCapability(NetworkCapabilities.TRANSPORT_WIFI) } returns false
        every { mockNetworkCapabilities.hasCapability(NetworkCapabilities.TRANSPORT_ETHERNET) } returns true
        every { mockConnectivityManager.getNetworkCapabilities(mockConnectivityManager.activeNetwork) } returns mockNetworkCapabilities

        // Act
        hasNetworkConnection(mockContext)

        // Assert
        verify { mockNetworkCapabilities.hasCapability(NetworkCapabilities.TRANSPORT_ETHERNET) }
    }*/

/*    @Test
    fun `verify has network connection false when all capabilities are false`() {
        clearAllMocks()
        // Arrange
        every { mockContext.getSystemService(Context.CONNECTIVITY_SERVICE) } returns mockConnectivityManager
        every { mockNetworkCapabilities.hasCapability(NetworkCapabilities.TRANSPORT_CELLULAR) } returns false
        every { mockNetworkCapabilities.hasCapability(NetworkCapabilities.TRANSPORT_WIFI) } returns false
        every { mockNetworkCapabilities.hasCapability(NetworkCapabilities.TRANSPORT_ETHERNET) } returns false
        every { mockConnectivityManager.getNetworkCapabilities(mockConnectivityManager.activeNetwork) } returns mockNetworkCapabilities

        // Act
        hasNetworkConnection(mockContext)

        // Assert
        verify { !mockNetworkCapabilities.hasCapability(NetworkCapabilities.TRANSPORT_CELLULAR) }
        verify { !mockNetworkCapabilities.hasCapability(NetworkCapabilities.TRANSPORT_WIFI) }
        verify { !mockNetworkCapabilities.hasCapability(NetworkCapabilities.TRANSPORT_ETHERNET) }
    }*/

    @Test
    fun `verify has network connection false when capabilities is null`() {
        // Arrange
        every { mockContext.getSystemService(Context.CONNECTIVITY_SERVICE) } returns mockConnectivityManager
        every { mockConnectivityManager.getNetworkCapabilities(mockConnectivityManager.activeNetwork) } returns null

        // Act
        val hasNetwork = hasNetworkConnection(mockContext)

        // Assert
        assert(!hasNetwork)
    }
}
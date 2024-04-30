package com.mccarty.ritmo.repository.remote

import com.mccarty.networkrequest.network.NetworkRequest
import com.mccarty.ritmo.model.payload.RecentlyPlayedItem
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

interface RepositoryInt {
    suspend fun recentlyPlayedMusic(): Flow<NetworkRequest<RecentlyPlayedItem>>
}
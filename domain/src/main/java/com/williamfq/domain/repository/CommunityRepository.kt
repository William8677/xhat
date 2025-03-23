package com.williamfq.domain.repository

import com.williamfq.domain.model.Community
import kotlinx.coroutines.flow.Flow

interface CommunityRepository {
    fun getCommunities(): Flow<List<Community>>
    fun getUserCommunities(): Flow<List<Community>>
    fun getTrendingCommunities(): Flow<List<Community>>
    fun searchCommunities(query: String): Flow<List<Community>>

    suspend fun createCommunity(community: Community): Result<Unit>
    suspend fun joinCommunity(communityId: String): Result<Unit>
    suspend fun leaveCommunity(communityId: String): Result<Unit>
    suspend fun subscribeCommunity(communityId: String): Result<Unit>
}

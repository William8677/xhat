
package com.williamfq.domain.usecases

import com.williamfq.domain.model.Community
import com.williamfq.domain.repository.CommunityRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetCommunitiesUseCase @Inject constructor(
    private val repository: CommunityRepository
) {
    suspend operator fun invoke(): Flow<List<Community>> {
        return repository.getCommunities()
    }
}

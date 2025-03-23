
package com.williamfq.domain.usecases

import com.williamfq.domain.model.Community
import com.williamfq.domain.repository.CommunityRepository
import javax.inject.Inject

class CreateCommunityUseCase @Inject constructor(
    private val repository: CommunityRepository
) {
    suspend operator fun invoke(community: Community): Result<Unit> {
        return repository.createCommunity(community)
    }
}

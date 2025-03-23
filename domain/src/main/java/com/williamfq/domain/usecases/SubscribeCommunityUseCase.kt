// Archivo: com/williamfq/xhat/domain/usecase/SubscribeCommunityUseCase.kt
package com.williamfq.domain.usecases

import com.williamfq.domain.repository.CommunityRepository
import javax.inject.Inject

class SubscribeCommunityUseCase @Inject constructor(
    private val repository: CommunityRepository
) {
    suspend operator fun invoke(communityId: String): Result<Unit> {
        return repository.subscribeCommunity(communityId)
    }
}

package com.williamfq.domain.model

import com.williamfq.domain.model.Community

/**
 * Representa el resultado de la operación de comunidades.
 */
sealed class CommunityResult {
    data class Success(val communities: List<Community>) : CommunityResult()
    data class Error(val message: String) : CommunityResult()
    object Loading : CommunityResult()
}

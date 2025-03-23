package com.williamfq.domain.usecases

import com.williamfq.domain.repository.UserRepository
import javax.inject.Inject

/**
 * Caso de uso para obtener el ID del usuario actual
 * Created by William8677 on 2025-01-04 02:50:25
 */
class GetUserIdUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    operator fun invoke(): String {
        return userRepository.getCurrentUserId()
    }
}
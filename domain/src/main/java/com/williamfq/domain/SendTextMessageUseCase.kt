package com.williamfq.domain

import com.williamfq.domain.repository.UserActivityRepository

class SendTextMessageUseCase(private val repository: UserActivityRepository) {

    suspend operator fun invoke(userId: String, period: String): Result<Unit> {
        return repository.incrementTextMessageCount(userId, period)
    }
}

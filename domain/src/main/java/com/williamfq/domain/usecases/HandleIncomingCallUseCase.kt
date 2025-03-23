package com.williamfq.domain.usecases

import com.williamfq.domain.repository.CallRepository
import javax.inject.Inject

class HandleIncomingCallUseCase @Inject constructor(
    private val callRepository: CallRepository
) {
    fun invoke(callerId: String) {
        callRepository.logIncomingCall(callerId)
    }
}

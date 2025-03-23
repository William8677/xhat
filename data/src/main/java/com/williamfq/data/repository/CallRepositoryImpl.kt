package com.williamfq.data.repository

import com.williamfq.domain.repository.CallRepository
import javax.inject.Inject

class CallRepositoryImpl @Inject constructor() : CallRepository {
    override fun logIncomingCall(callerId: String) {
        // Lógica para manejar llamadas
        println("Llamada registrada: $callerId")
    }
}

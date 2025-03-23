package com.williamfq.domain.repository

interface CallRepository {
    fun logIncomingCall(callerId: String)
}

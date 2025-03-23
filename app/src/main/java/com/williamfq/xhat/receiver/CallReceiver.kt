package com.williamfq.xhat.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.williamfq.domain.usecases.HandleIncomingCallUseCase
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class CallReceiver : BroadcastReceiver() {

    @Inject
    lateinit var handleIncomingCallUseCase: HandleIncomingCallUseCase

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (action == "com.williamfq.xhat.INCOMING_CALL") {
            val callerId = intent.getStringExtra("caller_id") ?: "Unknown"
            handleIncomingCallUseCase.invoke(callerId)
        }
    }
}

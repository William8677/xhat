package com.williamfq.xhat.service

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder

class ARVideoEffectService : Service() {

    private val binder = ARVideoEffectBinder()

    inner class ARVideoEffectBinder : Binder() {
        fun getService(): ARVideoEffectService = this@ARVideoEffectService
    }

    override fun onBind(intent: Intent?): IBinder = binder

    fun applyAREffect(effectName: String) {
        // Lógica para aplicar efectos de realidad aumentada
    }
}

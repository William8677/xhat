/*
 * Updated: 2025-01-25 15:52:11
 * Author: William8677
 */

package com.williamfq.xhat.service.video

import android.content.Context
import javax.inject.Inject

class CameraManagerImpl @Inject constructor(
    private val context: Context
) : CameraManager {
    private var isCameraOn = false

    override fun startPreview() {
        isCameraOn = true
        // Implementar inicio de preview
    }

    override fun stopPreview() {
        isCameraOn = false
        // Implementar parada de preview
    }

    override fun switchCamera() {
        // Implementar cambio de cámara
    }

    override fun isCameraOn(): Boolean = isCameraOn
}
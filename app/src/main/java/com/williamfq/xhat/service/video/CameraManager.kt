/*
 * Updated: 2025-01-25 15:52:11
 * Author: William8677
 */

package com.williamfq.xhat.service.video

import android.content.Context
import javax.inject.Inject

interface CameraManager {
    fun startPreview()
    fun stopPreview()
    fun switchCamera()
    fun isCameraOn(): Boolean
}
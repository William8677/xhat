/*
 * Updated: 2025-02-13 01:00:54
 * Author: William8677
 */

package com.williamfq.xhat.call.recording

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import com.williamfq.xhat.utils.analytics.Analytics
import com.williamfq.xhat.utils.analytics.CallAnalyticsEvent
import com.williamfq.xhat.utils.logging.LoggerInterface
import com.williamfq.xhat.utils.logging.LogLevel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CallRecordingManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val analytics: Analytics,
    private val logger: LoggerInterface
) {
    private var mediaRecorder: MediaRecorder? = null
    private var currentRecordingFile: File? = null
    private val recordingScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val _recordingState = MutableStateFlow<RecordingState>(RecordingState.Idle)
    val recordingState: StateFlow<RecordingState> = _recordingState

    companion object {
        private const val TAG = "CallRecordingManager"
    }

    fun startRecording(callId: String) {
        if (_recordingState.value is RecordingState.Recording) return

        recordingScope.launch {
            try {
                val file = createRecordingFile(callId)
                currentRecordingFile = file

                mediaRecorder = createMediaRecorder(file)
                mediaRecorder?.apply {
                    prepare()
                    start()
                }

                _recordingState.value = RecordingState.Recording(
                    startTime = System.currentTimeMillis(),
                    filePath = file.absolutePath
                )

                analytics.trackEvent("call_recording_started")
                logger.logEvent(
                    tag = TAG,
                    message = "Recording started for call: $callId",
                    level = LogLevel.INFO
                )
            } catch (e: Exception) {
                handleRecordingError("start_recording", e)
            }
        }
    }

    fun stopRecording() {
        recordingScope.launch {
            try {
                mediaRecorder?.apply {
                    stop()
                    release()
                }
                mediaRecorder = null

                val recordingState = _recordingState.value
                if (recordingState is RecordingState.Recording) {
                    val duration = System.currentTimeMillis() - recordingState.startTime
                    _recordingState.value = RecordingState.Completed(
                        filePath = recordingState.filePath,
                        duration = duration
                    )

                    analytics.trackEvent("call_recording_completed")
                    logger.logEvent(
                        tag = TAG,
                        message = "Recording completed. Duration: $duration ms",
                        level = LogLevel.INFO
                    )
                }
            } catch (e: Exception) {
                handleRecordingError("stop_recording", e)
            } finally {
                _recordingState.value = RecordingState.Idle
            }
        }
    }

    private fun createMediaRecorder(file: File): MediaRecorder {
        return (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(context)
        } else {
            @Suppress("DEPRECATION")
            MediaRecorder()
        }).apply {
            setAudioSource(MediaRecorder.AudioSource.VOICE_COMMUNICATION)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setAudioEncodingBitRate(128000)
            setAudioSamplingRate(44100)
            setOutputFile(file.absolutePath)
        }
    }

    private fun createRecordingFile(callId: String): File {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val fileName = "call_${callId}_${timestamp}.mp4"
        val recordingsDir = File(context.filesDir, "call_recordings").apply {
            if (!exists()) mkdirs()
        }
        return File(recordingsDir, fileName)
    }

    fun deleteRecording(filePath: String) {
        recordingScope.launch {
            try {
                File(filePath).delete()
                analytics.trackEvent("call_recording_deleted")
                logger.logEvent(
                    tag = TAG,
                    message = "Recording deleted: $filePath",
                    level = LogLevel.INFO
                )
            } catch (e: Exception) {
                handleRecordingError("delete_recording", e)
            }
        }
    }

    private suspend fun handleRecordingError(operation: String, error: Exception) {
        _recordingState.value = RecordingState.Error(
            message = "Error in $operation: ${error.message}"
        )
        logger.logEvent(
            tag = TAG,
            message = "Recording error in $operation",
            level = LogLevel.ERROR,
            throwable = error
        )
        analytics.trackEvent("call_recording_error")
    }
}

sealed class RecordingState {
    object Idle : RecordingState()

    data class Recording(
        val startTime: Long,
        val filePath: String
    ) : RecordingState()

    data class Completed(
        val filePath: String,
        val duration: Long
    ) : RecordingState()

    data class Error(
        val message: String
    ) : RecordingState()
}
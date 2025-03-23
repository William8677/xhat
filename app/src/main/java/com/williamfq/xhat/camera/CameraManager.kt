/*
 * Updated: 2025-01-27 21:43:52
 * Author: William8677
 */

package com.williamfq.xhat.camera

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.graphics.Rect
import android.graphics.YuvImage
import android.hardware.camera2.*
import android.media.Image
import android.media.ImageReader
import android.os.Handler
import android.os.HandlerThread
import android.util.Size
import android.view.Surface
import androidx.core.content.ContextCompat
import com.williamfq.xhat.service.filter.FilterProcessor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.ByteArrayOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CameraManager @Inject constructor(
    private val context: Context,
    private val filterProcessor: FilterProcessor
) {
    private val cameraManager: android.hardware.camera2.CameraManager =
        context.getSystemService(Context.CAMERA_SERVICE) as android.hardware.camera2.CameraManager
    private var cameraDevice: CameraDevice? = null
    private var captureSession: CameraCaptureSession? = null
    private var imageReader: ImageReader? = null
    private var backgroundHandler: Handler? = null
    private var backgroundThread: HandlerThread? = null
    private var previewSurface: Surface? = null

    private val coroutineScope = CoroutineScope(Dispatchers.Default)

    private val _cameraState = MutableStateFlow<CameraState>(CameraState.Idle)
    val cameraState: StateFlow<CameraState> = _cameraState

    private var currentFacingMode = CameraFacing.FRONT

    init {
        startBackgroundThread()
    }

    fun startCamera(surface: Surface, width: Int, height: Int) {
        previewSurface = surface
        val cameraId = getFacingCameraId(currentFacingMode)
        if (cameraId == null) {
            _cameraState.value = CameraState.Error("No se encontró una cámara compatible")
            return
        }

        setupImageReader(width, height)
        openCamera(cameraId)
    }

    private fun setupImageReader(width: Int, height: Int) {
        imageReader?.close()
        imageReader = ImageReader.newInstance(
            width, height,
            ImageFormat.YUV_420_888,
            2
        ).apply {
            setOnImageAvailableListener({ reader ->
                val image = reader.acquireLatestImage()
                try {
                    processImage(image)
                } finally {
                    image?.close()
                }
            }, backgroundHandler)
        }
    }

    private fun processImage(image: Image?) {
        image ?: return
        try {
            val bitmap = imageToBitmap(image)

            coroutineScope.launch {
                try {
                    val processedBitmap = filterProcessor.processImage(bitmap)
                    // Aquí puedes hacer algo con el bitmap procesado si es necesario
                } catch (e: Exception) {
                    Timber.e(e, "Error procesando imagen con filtro")
                } finally {
                    bitmap.recycle()
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Error convirtiendo imagen")
        }
    }

    private fun imageToBitmap(image: Image): Bitmap {
        val yBuffer = image.planes[0].buffer
        val uBuffer = image.planes[1].buffer
        val vBuffer = image.planes[2].buffer

        val ySize = yBuffer.remaining()
        val uSize = uBuffer.remaining()
        val vSize = vBuffer.remaining()

        val nv21 = ByteArray(ySize + uSize + vSize)

        yBuffer.get(nv21, 0, ySize)
        vBuffer.get(nv21, ySize, vSize)
        uBuffer.get(nv21, ySize + vSize, uSize)

        val yuvImage = YuvImage(nv21, ImageFormat.NV21, image.width, image.height, null)
        val out = ByteArrayOutputStream()
        yuvImage.compressToJpeg(Rect(0, 0, image.width, image.height), 100, out)
        val imageBytes = out.toByteArray()
        return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
    }

    @SuppressLint("MissingPermission")
    private fun openCamera(cameraId: String) {
        try {
            if (ContextCompat.checkSelfPermission(
                    context,
                    android.Manifest.permission.CAMERA
                ) != android.content.pm.PackageManager.PERMISSION_GRANTED
            ) {
                _cameraState.value = CameraState.Error("Permisos de cámara no concedidos")
                return
            }

            val callback = object : CameraDevice.StateCallback() {
                override fun onOpened(camera: CameraDevice) {
                    cameraDevice = camera
                    previewSurface?.let { createCameraPreviewSession(it) }
                    _cameraState.value = CameraState.Preview
                }

                override fun onDisconnected(camera: CameraDevice) {
                    camera.close()
                    cameraDevice = null
                    _cameraState.value = CameraState.Idle
                }

                override fun onError(camera: CameraDevice, error: Int) {
                    camera.close()
                    cameraDevice = null
                    _cameraState.value = CameraState.Error("Error al abrir la cámara: $error")
                }
            }

            cameraManager.openCamera(cameraId, callback, backgroundHandler)
        } catch (e: CameraAccessException) {
            _cameraState.value = CameraState.Error("Error al acceder a la cámara: ${e.message}")
        }
    }

    private fun createCameraPreviewSession(surface: Surface) {
        try {
            val surfaces = mutableListOf<Surface>().apply {
                add(surface)
                imageReader?.surface?.let { add(it) }
            }

            val previewRequestBuilder = cameraDevice?.createCaptureRequest(
                CameraDevice.TEMPLATE_PREVIEW
            )?.apply {
                surfaces.forEach { addTarget(it) }
            } ?: return

            cameraDevice?.createCaptureSession(
                surfaces,
                object : CameraCaptureSession.StateCallback() {
                    override fun onConfigured(session: CameraCaptureSession) {
                        captureSession = session
                        try {
                            session.setRepeatingRequest(
                                previewRequestBuilder.build(),
                                null,
                                backgroundHandler
                            )
                        } catch (e: CameraAccessException) {
                            _cameraState.value = CameraState.Error("Error en la sesión de captura: ${e.message}")
                        }
                    }

                    override fun onConfigureFailed(session: CameraCaptureSession) {
                        _cameraState.value = CameraState.Error("Error al configurar la sesión de cámara")
                    }
                },
                backgroundHandler
            )
        } catch (e: CameraAccessException) {
            _cameraState.value = CameraState.Error("Error al crear la sesión de vista previa: ${e.message}")
        }
    }

    fun switchCamera() {
        currentFacingMode = when (currentFacingMode) {
            CameraFacing.FRONT -> CameraFacing.BACK
            CameraFacing.BACK -> CameraFacing.FRONT
        }

        stopCamera()
        previewSurface?.let { surface ->
            startCamera(surface, PREVIEW_SIZE.width, PREVIEW_SIZE.height)
        }
    }

    fun stopCamera() {
        try {
            captureSession?.close()
            captureSession = null

            cameraDevice?.close()
            cameraDevice = null

            imageReader?.close()
            imageReader = null

            _cameraState.value = CameraState.Idle
        } catch (e: Exception) {
            _cameraState.value = CameraState.Error("Error al detener la cámara: ${e.message}")
        }
    }

    private fun startBackgroundThread() {
        backgroundThread = HandlerThread("CameraBackground").also { it.start() }
        backgroundHandler = backgroundThread?.looper?.let { Handler(it) }
    }

    private fun stopBackgroundThread() {
        backgroundThread?.quitSafely()
        try {
            backgroundThread?.join()
            backgroundThread = null
            backgroundHandler = null
        } catch (e: InterruptedException) {
            _cameraState.value = CameraState.Error("Error al detener el hilo de fondo: ${e.message}")
        }
    }

    private fun getFacingCameraId(facing: CameraFacing): String? {
        return try {
            val cameraList = cameraManager.cameraIdList.toList()

            cameraList.find { id ->
                val characteristics = cameraManager.getCameraCharacteristics(id)
                val cameraFacing = characteristics.get(CameraCharacteristics.LENS_FACING)
                when (facing) {
                    CameraFacing.FRONT -> cameraFacing == CameraCharacteristics.LENS_FACING_FRONT
                    CameraFacing.BACK -> cameraFacing == CameraCharacteristics.LENS_FACING_BACK
                }
            }
        } catch (e: CameraAccessException) {
            Timber.e(e, "Error al obtener ID de cámara")
            null
        }
    }

    fun release() {
        stopCamera()
        stopBackgroundThread()
        filterProcessor.release()
    }

    sealed class CameraState {
        object Idle : CameraState()
        object Preview : CameraState()
        data class Error(val message: String) : CameraState()
    }

    enum class CameraFacing {
        FRONT, BACK
    }

    companion object {
        private val PREVIEW_SIZE = Size(1080, 1920)
    }
}
package com.williamfq.xhat.ui.camera

import android.view.SurfaceHolder
import android.view.SurfaceView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.*
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import com.williamfq.xhat.ui.camera.components.*  // Se asume que aquí se definen CameraPreview, CameraGrid, etc.
import com.williamfq.xhat.ui.camera.viewmodel.CameraViewModel
// Importar FilterType desde el paquete correcto:
import com.williamfq.xhat.domain.model.FilterType
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

// Declaración top-level de allFilters para evitar errores (se asume que FilterType es un enum)
private val allFilters: List<FilterType> = FilterType.values().toList()

/**
 * Pantalla principal que muestra la cámara y controles.
 */
@Composable
fun CameraScreen(
    onNavigateToGallery: () -> Unit,
    viewModel: CameraViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // Control para mostrar/ocultar el bottom sheet de filtros
    var showFilters by remember { mutableStateOf(false) }
    // Estado para la posición de foco (para FocusCircle)
    var focusPosition by remember { mutableStateOf<Offset?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            // Captura toques en la preview para enfocar manualmente
            .pointerInput(Unit) {
                coroutineScope {
                    while (true) {
                        // Esperamos el primer toque
                        val down = awaitPointerEventScope { awaitFirstDown() }
                        focusPosition = down.position
                    }
                }
            }
    ) {
        // 1) Preview de la cámara
        CameraPreview(
            onSurfaceProvided = { surface, width, height ->
                // Lógica para iniciar la cámara usando tu ViewModel o CameraManager
                viewModel.startCamera(surface, width, height)
            }
        )

        // 2) Cuadrícula de la cámara (opcional)
        CameraGrid()

        // 3) Controles superiores (flash y cambio de cámara)
        TopControls(
            isFlashOn = uiState.isFlashOn,
            onFlashToggle = { viewModel.toggleFlash() },
            onSwitchCamera = { viewModel.switchCamera() },
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(16.dp)
        )

        // 4) Controles inferiores (captura, galería, filtros)
        BottomControls(
            isCapturing = uiState.isCapturing,
            onCaptureClick = { viewModel.capturePhoto() },
            onGalleryClick = onNavigateToGallery,
            onFiltersClick = { showFilters = true },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp)
        )

        // 5) Indicador de “procesando filtro”
        if (uiState.isFilterProcessing) {
            CircularProgressIndicator(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(48.dp)
            )
        }

        // 6) BottomSheet de filtros (ejemplo)
        if (showFilters) {
            FilterBottomSheet(
                currentFilter = uiState.currentFilter,
                onFilterSelected = { filter ->
                    // filter es un FilterType? -> lo pasamos al viewModel
                    viewModel.toggleFilter(filter)
                },
                onDismiss = { showFilters = false }
            )
        }

        // 7) Círculo de enfoque (FocusCircle)
        focusPosition?.let { pos ->
            FocusCircle(
                position = pos,
                onAnimationEnd = {
                    // Ocultamos el círculo tras la animación
                    focusPosition = null
                }
            )
        }

        // 8) Mensajes de error con animación
        AnimatedVisibility(
            visible = uiState.error != null || uiState.filterError != null,
            enter = fadeIn() + slideInVertically(),
            exit = fadeOut() + slideOutVertically(),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 72.dp)
        ) {
            ErrorMessage(
                message = uiState.error ?: uiState.filterError ?: "",
                onDismiss = { viewModel.clearError() }
            )
        }
    }
}

/**
 * Ejemplo de hoja de filtros que maneja FilterType? en lugar de String?
 * Ajusta según tu lógica real de filtros.
 */
@Composable
fun FilterBottomSheet(
    currentFilter: FilterType?,              // Acepta FilterType?
    onFilterSelected: (FilterType?) -> Unit, // Callback con FilterType?
    onDismiss: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant,
            shadowElevation = 8.dp,
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .align(Alignment.BottomCenter)
        ) {
            Column(Modifier.padding(16.dp)) {
                Text("Seleccionar Filtro", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))

                // Se itera sobre allFilters con el tipo especificado para evitar ambigüedad
                allFilters.forEach { filterType: FilterType ->
                    TextButton(onClick = {
                        onFilterSelected(filterType)
                        onDismiss()
                    }) {
                        Text(text = "Filtro: ${filterType.name}")
                    }
                }

                // Opción para quitar filtro
                TextButton(onClick = {
                    onFilterSelected(null)
                    onDismiss()
                }) {
                    Text(text = "Sin Filtro")
                }

                Spacer(modifier = Modifier.weight(1f))
                TextButton(onClick = onDismiss) {
                    Text("Cerrar")
                }
            }
        }
    }
}

/**
 * Stub composable para TopControls.
 * Se simula una fila con botones para togglear el flash y cambiar la cámara.
 */
@Composable
fun TopControls(
    isFlashOn: Boolean,
    onFlashToggle: () -> Unit,
    onSwitchCamera: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        IconButton(onClick = onFlashToggle) {
            Icon(
                imageVector = if (isFlashOn) Icons.Default.FlashOn else Icons.Default.FlashOff,
                contentDescription = "Toggle Flash"
            )
        }
        IconButton(onClick = onSwitchCamera) {
            Icon(
                imageVector = Icons.Default.Cached,
                contentDescription = "Switch Camera"
            )
        }
    }
}

/**
 * Stub composable para BottomControls.
 * Se simula una fila con botones para capturar, abrir galería y mostrar filtros.
 */
@Composable
fun BottomControls(
    isCapturing: Boolean,
    onCaptureClick: () -> Unit,
    onGalleryClick: () -> Unit,
    onFiltersClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onCaptureClick) {
            Icon(Icons.Default.Camera, contentDescription = "Capture")
        }
        IconButton(onClick = onGalleryClick) {
            Icon(Icons.Default.PhotoLibrary, contentDescription = "Gallery")
        }
        IconButton(onClick = onFiltersClick) {
            Icon(Icons.Default.Filter, contentDescription = "Filters")
        }
    }
}

/**
 * Stub composable para FocusCircle.
 * Dibuja un círculo en la posición indicada y simula una animación.
 */
@Composable
fun FocusCircle(
    position: Offset,
    onAnimationEnd: () -> Unit
) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        drawCircle(
            color = Color.Yellow,
            radius = 50f,
            center = position,
            style = Stroke(width = 4f)
        )
    }
    LaunchedEffect(Unit) {
        delay(500)
        onAnimationEnd()
    }
}

/**
 * Stub composable para ErrorMessage.
 * Muestra un mensaje de error en la parte superior.
 */
@Composable
fun ErrorMessage(
    message: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = MaterialTheme.colorScheme.errorContainer,
        shape = MaterialTheme.shapes.medium,
        modifier = modifier.padding(horizontal = 16.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(8.dp)
        ) {
            Text(
                text = message,
                color = MaterialTheme.colorScheme.onErrorContainer,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = onDismiss) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Cerrar",
                    tint = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }
    }
}

/*
 * Updated: 2025-01-21 20:06:45
 * Author: William8677
 */

package com.williamfq.xhat.ui.filters.components.controls

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.williamfq.xhat.R
import com.williamfq.xhat.domain.model.FilterType
import com.williamfq.xhat.utils.ar.ARSystem

// Stub para AREffectAsset
data class AREffectAsset(val id: String)

// Stub para AREffectItem composable
@Composable
fun AREffectItem(
    asset: AREffectAsset,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Surface(
        onClick = onSelect,
        modifier = Modifier.size(64.dp),
        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            Text(text = asset.id, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

// Stub para DefaultEffectControls
@Composable
fun DefaultEffectControls(
    filter: FilterType,
    onParameterChange: (String, Float) -> Unit
) {
    Text("No additional controls", style = MaterialTheme.typography.bodySmall)
}

@Composable
fun BeautyControls(
    filter: FilterType,
    onParameterChange: (String, Float) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        FilterParameterSlider(
            label = "Suavizado",
            value = filter.parameters["smoothing"] as? Float ?: 0.5f,
            onValueChange = { onParameterChange("smoothing", it) }
        )
        FilterParameterSlider(
            label = "Brillo",
            value = filter.parameters["brightness"] as? Float ?: 0f,
            onValueChange = { onParameterChange("brightness", it) },
            valueRange = -0.5f..0.5f
        )
        FilterParameterSlider(
            label = "Contraste",
            value = filter.parameters["contrast"] as? Float ?: 1f,
            onValueChange = { onParameterChange("contrast", it) },
            valueRange = 0.5f..1.5f
        )
    }
}

@Composable
fun FunControls(
    filter: FilterType,
    arSystem: ARSystem,
    onAREffectSelected: (String) -> Unit = {},
    onParameterChange: (String, Float) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "Efectos AR",
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 16.dp)
        ) {
            // Convertimos la lista de ARSystem.ARAsset a AREffectAsset
            itemsIndexed(arSystem.getAvailableAssets().map { arAsset ->
                AREffectAsset(arAsset.id)
            }) { index, effectAsset ->
                AREffectItem(
                    asset = effectAsset,
                    isSelected = (filter.parameters["arEffect"]?.toString() == effectAsset.id),
                    onSelect = { onAREffectSelected(effectAsset.id) }
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        if (filter.parameters["animated"] == true) {
            FilterParameterSlider(
                label = "Velocidad de animación",
                value = filter.parameters["animationSpeed"] as? Float ?: 1f,
                onValueChange = { onParameterChange("animationSpeed", it) },
                valueRange = 0.5f..2f
            )
        }
    }
}


@Composable
fun ArtisticControls(
    filter: FilterType,
    onParameterChange: (String, Float) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        FilterParameterSlider(
            label = "Intensidad artística",
            value = filter.parameters["artisticIntensity"] as? Float ?: 0.5f,
            onValueChange = { onParameterChange("artisticIntensity", it) }
        )
        FilterParameterSlider(
            label = "Saturación",
            value = filter.parameters["saturation"] as? Float ?: 1f,
            onValueChange = { onParameterChange("saturation", it) },
            valueRange = 0f..2f
        )
        ColorMixerControl(
            selectedColor = filter.parameters["colorTint"] as? Color ?: Color.Transparent,
            onColorSelected = { onParameterChange("colorTint", it.value.toFloat()) }
        )
    }
}

@Composable
fun EffectsControls(
    filter: FilterType,
    onParameterChange: (String, Float) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        when (filter.name) {
            "Glitter" -> GlitterControls(filter, onParameterChange)
            "Neon" -> NeonControls(filter, onParameterChange)
            "Galaxy" -> GalaxyControls(filter, onParameterChange)
            "Hearts" -> HeartsControls(filter, onParameterChange)
            else -> DefaultEffectControls(filter, onParameterChange)
        }
    }
}

@Composable
private fun GlitterControls(
    filter: FilterType,
    onParameterChange: (String, Float) -> Unit
) {
    Column {
        FilterParameterSlider(
            label = "Densidad",
            value = filter.parameters["glitterDensity"] as? Float ?: 0.5f,
            onValueChange = { onParameterChange("glitterDensity", it) }
        )
        FilterParameterSlider(
            label = "Tamaño",
            value = filter.parameters["glitterSize"] as? Float ?: 0.5f,
            onValueChange = { onParameterChange("glitterSize", it) }
        )
        FilterParameterSlider(
            label = "Velocidad",
            value = filter.parameters["glitterSpeed"] as? Float ?: 1f,
            onValueChange = { onParameterChange("glitterSpeed", it) },
            valueRange = 0.1f..2f
        )
    }
}

@Composable
private fun NeonControls(
    filter: FilterType,
    onParameterChange: (String, Float) -> Unit
) {
    Column {
        FilterParameterSlider(
            label = "Brillo",
            value = filter.parameters["neonGlow"] as? Float ?: 0.5f,
            onValueChange = { onParameterChange("neonGlow", it) }
        )
        ColorMixerControl(
            selectedColor = filter.parameters["neonColor"] as? Color ?: Color.Cyan,
            onColorSelected = { onParameterChange("neonColor", it.value.toFloat()) }
        )
    }
}

@Composable
private fun GalaxyControls(
    filter: FilterType,
    onParameterChange: (String, Float) -> Unit
) {
    Column {
        FilterParameterSlider(
            label = "Estrellas",
            value = filter.parameters["starDensity"] as? Float ?: 0.5f,
            onValueChange = { onParameterChange("starDensity", it) }
        )
        FilterParameterSlider(
            label = "Rotación",
            value = filter.parameters["galaxyRotation"] as? Float ?: 1f,
            onValueChange = { onParameterChange("galaxyRotation", it) },
            valueRange = 0.1f..2f
        )
    }
}

@Composable
private fun HeartsControls(
    filter: FilterType,
    onParameterChange: (String, Float) -> Unit
) {
    Column {
        FilterParameterSlider(
            label = "Tamaño",
            value = filter.parameters["heartSize"] as? Float ?: 0.5f,
            onValueChange = { onParameterChange("heartSize", it) }
        )
        FilterParameterSlider(
            label = "Velocidad",
            value = filter.parameters["heartSpeed"] as? Float ?: 1f,
            onValueChange = { onParameterChange("heartSpeed", it) },
            valueRange = 0.1f..2f
        )
        ColorMixerControl(
            selectedColor = filter.parameters["heartColor"] as? Color ?: Color.Red,
            onColorSelected = { onParameterChange("heartColor", it.value.toFloat()) }
        )
    }
}

@Composable
private fun FilterParameterSlider(
    label: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium
        )
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            steps = 100
        )
    }
}

@Composable
private fun ColorMixerControl(
    selectedColor: Color,
    onColorSelected: (Color) -> Unit,
    modifier: Modifier = Modifier
) {
    var showColorPicker by remember { mutableStateOf(false) }
    Column(modifier = modifier) {
        Text(
            text = "Color",
            style = MaterialTheme.typography.bodyMedium
        )
        Button(
            onClick = { showColorPicker = true },
            colors = ButtonDefaults.buttonColors(
                containerColor = selectedColor
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Text("Seleccionar color")
        }
        if (showColorPicker) {
            ColorPickerDialog(
                initialColor = selectedColor,
                onColorSelected = {
                    onColorSelected(it)
                    showColorPicker = false
                },
                onDismiss = { showColorPicker = false }
            )
        }
    }
}

@Composable
private fun ColorPickerDialog(
    initialColor: Color,
    onColorSelected: (Color) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Seleccionar color") },
        confirmButton = {
            TextButton(onClick = { onColorSelected(initialColor) }) {
                Text("Aceptar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        },
        text = {
            ColorWheel(
                color = initialColor,
                onColorChange = onColorSelected
            )
        }
    )
}

@Composable
private fun ColorWheel(
    color: Color,
    onColorChange: (Color) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(100.dp)
            .background(color),
        contentAlignment = Alignment.Center
    ) {
        Text("Color Wheel", color = Color.White)
    }
}

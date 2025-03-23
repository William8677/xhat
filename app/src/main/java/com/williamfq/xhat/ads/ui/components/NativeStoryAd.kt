package com.williamfq.xhat.ads.ui.components

import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.nativead.MediaView
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView
import com.williamfq.xhat.core.config.AdMobConfig
import kotlinx.coroutines.delay
import android.view.ViewGroup.LayoutParams
import timber.log.Timber

@Composable
fun NativeStoryAd(
    nativeAd: NativeAd,
    onAdComplete: () -> Unit,
    onAdSkipped: () -> Unit
) {
    val context = LocalContext.current
    var canSkip by remember { mutableStateOf(false) }
    var remainingTime by remember { mutableLongStateOf(5L) }

    LaunchedEffect(Unit) {
        delay(AdMobConfig.MIN_TIME_TO_SKIP_AD_MS.toLong())
        canSkip = true
    }

    LaunchedEffect(Unit) {
        while (remainingTime > 0) {
            delay(1000L)
            remainingTime -= 1
        }
        onAdComplete()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Anuncio",
                    style = MaterialTheme.typography.labelMedium
                )
                if (canSkip) {
                    TextButton(onClick = onAdSkipped) {
                        Text("Saltar")
                        Icon(Icons.Default.Close, contentDescription = "Saltar anuncio")
                    }
                } else {
                    Text("Saltar en $remainingTime s")
                }
            }

            NativeAdContent(nativeAd)

            Button(
                onClick = {
                    nativeAd.callToAction?.let {
                        // Aquí se podría abrir una URL o realizar una acción personalizada
                        Timber.d("CTA clicked: $it")
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(nativeAd.callToAction ?: "Más información")
            }
        }
    }
}

@Composable
private fun NativeAdContent(nativeAd: NativeAd) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = nativeAd.headline ?: "Anuncio",
            style = MaterialTheme.typography.headlineMedium
        )

        nativeAd.body?.let { body ->
            Text(
                text = body,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        nativeAd.mediaContent?.let { mediaContent ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(9f / 16f)
            ) {
                AndroidView(
                    factory = { ctx ->
                        NativeAdView(ctx).apply {
                            mediaView = MediaView(ctx).apply {
                                layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
                                setMediaContent(mediaContent)
                            }
                            iconView = ImageView(ctx).apply {
                                nativeAd.icon?.drawable?.let { setImageDrawable(it); visibility = View.VISIBLE }
                                    ?: run { visibility = View.GONE }
                            }
                            headlineView = TextView(ctx).apply { text = nativeAd.headline }
                            bodyView = TextView(ctx).apply { text = nativeAd.body }
                            callToActionView = Button(ctx).apply { text = nativeAd.callToAction ?: "Más información" }
                            setNativeAd(nativeAd)
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            nativeAd.advertiser?.let { Text(it, style = MaterialTheme.typography.labelSmall) }
            nativeAd.starRating?.let { rating ->
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    repeat(rating.toInt()) {
                        Icon(Icons.Filled.Star, "Estrella", tint = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    }
}
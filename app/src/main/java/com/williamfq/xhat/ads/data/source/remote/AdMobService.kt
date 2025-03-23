package com.williamfq.xhat.ads.data.source.remote

import android.content.Context
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MediaAspectRatio
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import com.google.android.gms.ads.VideoOptions
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.williamfq.xhat.core.config.AdMobConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AdMobService @Inject constructor(
    @ApplicationContext private val context: Context
) {
    init {
        try {
            val config = RequestConfiguration.Builder()
                .setTestDeviceIds(listOf("TEST-DEVICE-ID")) // Reemplaza con tu ID de dispositivo de prueba real
                .build()
            MobileAds.setRequestConfiguration(config)
            MobileAds.initialize(context) {
                Timber.d("AdMob inicializado con éxito")
            }
        } catch (e: Exception) {
            Timber.e(e, "Error inicializando AdMob")
        }
    }

    fun loadNativeStoryAd(
        onAdLoaded: (NativeAd, AdLoader) -> Unit = { _, _ -> },
        onAdFailed: (LoadAdError) -> Unit = {}
    ): AdLoader {
        val builder = AdLoader.Builder(context, AdMobConfig.NATIVE_STORY_AD_UNIT_ID)
        val adLoader = builder
            .forNativeAd { nativeAd ->
                Timber.d("Native ad cargado: ${nativeAd.headline}")
                onAdLoaded(nativeAd, builder.build()) // Pasamos el AdLoader construido
            }
            .withAdListener(object : AdListener() {
                override fun onAdFailedToLoad(error: LoadAdError) {
                    Timber.e("Fallo al cargar anuncio: ${error.message}")
                    onAdFailed(error)
                }
                override fun onAdLoaded() {
                    Timber.d("Anuncio cargado exitosamente")
                }
            })
            .withNativeAdOptions(
                NativeAdOptions.Builder()
                    .setMediaAspectRatio(MediaAspectRatio.PORTRAIT)
                    .setVideoOptions(VideoOptions.Builder().setStartMuted(true).build())
                    .setAdChoicesPlacement(NativeAdOptions.ADCHOICES_TOP_RIGHT)
                    .build()
            )
            .build()

        try {
            adLoader.loadAd(com.google.android.gms.ads.AdRequest.Builder().build())
        } catch (e: Exception) {
            Timber.e(e, "Error cargando anuncio nativo")
        }
        return adLoader
    }
}
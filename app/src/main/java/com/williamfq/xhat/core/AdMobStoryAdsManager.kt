package com.williamfq.xhat.core

import android.content.Context
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds

class AdMobStoryAdsManager(private val context: Context) {

    fun initialize() {
        MobileAds.initialize(context)
    }

    fun loadAd(adView: AdView) {
        val adRequest = AdRequest.Builder().build()
        adView.loadAd(adRequest)
    }
}

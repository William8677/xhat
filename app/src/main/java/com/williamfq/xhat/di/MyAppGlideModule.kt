package com.williamfq.xhat.di

import android.content.Context
import com.bumptech.glide.Glide
import com.bumptech.glide.GlideBuilder
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.module.AppGlideModule

@GlideModule
class MyAppGlideModule : AppGlideModule() {
    override fun applyOptions(context: Context, builder: GlideBuilder) {
        super.applyOptions(context, builder)
        // Puedes personalizar las opciones de Glide aquí si lo deseas
    }

    override fun isManifestParsingEnabled(): Boolean {
        return false
    }
}

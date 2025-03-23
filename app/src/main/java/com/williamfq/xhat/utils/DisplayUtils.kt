package com.williamfq.xhat.utils

import android.widget.TextView
import android.util.Log
import timber.log.Timber

object DisplayUtils {

    // Método para mostrar subtítulos en un TextView
    fun displaySubtitle(subtitle: String, textView: TextView) {
        try {
            // Actualiza el texto en el TextView
            textView.text = subtitle
            Timber.tag("DisplayUtils").d("Subtítulo mostrado: $subtitle")
        } catch (e: Exception) {
            Timber.tag("DisplayUtils").e("Error al mostrar el subtítulo: ${e.message}")
        }
    }
}

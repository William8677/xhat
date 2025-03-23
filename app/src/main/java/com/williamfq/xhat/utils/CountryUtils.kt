package com.williamfq.xhat.utils

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.williamfq.domain.model.Country

fun loadCountries(context: Context): List<Country> {
    return try {
        context.assets.open("countries.json").bufferedReader().use { reader ->
            val json = reader.readText()
            val type = object : TypeToken<List<Country>>() {}.type
            Gson().fromJson(json, type)
        }
    } catch (e: Exception) {
        e.printStackTrace()
        emptyList()
    }
}

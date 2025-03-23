package com.williamfq.xhat.call.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import androidx.annotation.RequiresApi
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NetworkQualityMonitor @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    fun monitorNetworkQuality(): Flow<NetworkQuality> = callbackFlow {
        val callback = object : ConnectivityManager.NetworkCallback() {
            @RequiresApi(Build.VERSION_CODES.Q)
            override fun onCapabilitiesChanged(
                network: Network,
                networkCapabilities: NetworkCapabilities
            ) {
                val quality = when {
                    !networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) ->
                        NetworkQuality.NO_INTERNET

                    networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ->
                        if (networkCapabilities.signalStrength >= -50)
                            NetworkQuality.EXCELLENT
                        else if (networkCapabilities.signalStrength >= -60)
                            NetworkQuality.GOOD
                        else
                            NetworkQuality.POOR

                    networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ->
                        when {
                            networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_CONGESTED) &&
                                    networkCapabilities.linkDownstreamBandwidthKbps > 1000 ->
                                NetworkQuality.GOOD
                            networkCapabilities.linkDownstreamBandwidthKbps > 500 ->
                                NetworkQuality.FAIR
                            else ->
                                NetworkQuality.POOR
                        }

                    else -> NetworkQuality.POOR
                }

                trySend(quality)
            }

            override fun onLost(network: Network) {
                trySend(NetworkQuality.NO_INTERNET)
            }
        }

        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
            .build()

        connectivityManager.registerNetworkCallback(request, callback)

        awaitClose {
            connectivityManager.unregisterNetworkCallback(callback)
        }
    }
}

enum class NetworkQuality {
    EXCELLENT,
    GOOD,
    FAIR,
    POOR,
    NO_INTERNET
}
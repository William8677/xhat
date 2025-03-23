package com.williamfq.xhat.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import androidx.annotation.RequiresApi
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ConnectionManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    private val _connectionState = MutableStateFlow<ConnectionState>(ConnectionState.Unknown)
    val connectionState: StateFlow<ConnectionState> = _connectionState

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            val capabilities = connectivityManager.getNetworkCapabilities(network)
            updateConnectionState(capabilities)
            Timber.d("Network available: ${capabilities?.toString()}")
        }

        override fun onLost(network: Network) {
            _connectionState.value = ConnectionState.NoConnection
            Timber.w("Network lost")
        }

        override fun onCapabilitiesChanged(network: Network, capabilities: NetworkCapabilities) {
            updateConnectionState(capabilities)
            Timber.d("Network capabilities changed: $capabilities")
        }
    }

    fun startMonitoring() {
        try {
            val request = NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
                .build()

            connectivityManager.registerNetworkCallback(request, networkCallback)
            checkInitialConnection()
        } catch (e: Exception) {
            Timber.e(e, "Error starting network monitoring")
        }
    }

    fun stopMonitoring() {
        try {
            connectivityManager.unregisterNetworkCallback(networkCallback)
        } catch (e: Exception) {
            Timber.e(e, "Error stopping network monitoring")
        }
    }

    private fun checkInitialConnection() {
        val network = connectivityManager.activeNetwork
        val capabilities = connectivityManager.getNetworkCapabilities(network)
        updateConnectionState(capabilities)
    }

    private fun updateConnectionState(capabilities: NetworkCapabilities?) {
        val state = when {
            capabilities == null -> ConnectionState.NoConnection
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> {
                val hasInternet = capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                if (hasInternet) ConnectionState.Wifi else ConnectionState.NoConnection
            }
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> {
                val hasInternet = capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                if (hasInternet) ConnectionState.Cellular else ConnectionState.NoConnection
            }
            else -> ConnectionState.NoConnection
        }
        _connectionState.value = state
    }

    fun isConnected(): Boolean {
        return _connectionState.value.let { it is ConnectionState.Wifi || it is ConnectionState.Cellular }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    fun getConnectionQuality(): ConnectionQuality {
        val network = connectivityManager.activeNetwork
        val capabilities = connectivityManager.getNetworkCapabilities(network)

        return when {
            capabilities == null -> ConnectionQuality.POOR
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> {
                when {
                    capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED) -> ConnectionQuality.EXCELLENT
                    capabilities.signalStrength >= -50 -> ConnectionQuality.EXCELLENT
                    capabilities.signalStrength >= -60 -> ConnectionQuality.GOOD
                    capabilities.signalStrength >= -70 -> ConnectionQuality.FAIR
                    else -> ConnectionQuality.POOR
                }
            }
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> {
                when {
                    capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED) -> ConnectionQuality.GOOD
                    capabilities.signalStrength >= -85 -> ConnectionQuality.GOOD
                    capabilities.signalStrength >= -95 -> ConnectionQuality.FAIR
                    else -> ConnectionQuality.POOR
                }
            }
            else -> ConnectionQuality.POOR
        }
    }
}

sealed class ConnectionState {
    object Unknown : ConnectionState()
    object NoConnection : ConnectionState()
    object Wifi : ConnectionState()
    object Cellular : ConnectionState()
}

enum class ConnectionQuality {
    POOR,
    FAIR,
    GOOD,
    EXCELLENT
}
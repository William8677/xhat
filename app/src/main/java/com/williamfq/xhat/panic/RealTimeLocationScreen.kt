package com.williamfq.xhat.panic

import android.Manifest
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import com.williamfq.domain.location.LocationTracker
import com.williamfq.xhat.R
import com.williamfq.xhat.ui.Navigation.ChatType
import com.williamfq.xhat.ui.components.PermissionDialog
import kotlinx.coroutines.flow.catch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RealTimeLocationScreen(
    locationTracker: LocationTracker,
    chatId: String,
    chatType: ChatType,
    onNavigateBack: () -> Unit,
    viewModel: RealTimeLocationViewModel = hiltViewModel()
) {
    var showPermissionDialog by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }
    val cameraPositionState = rememberCameraPositionState()
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        locationTracker.getCurrentLocation()
            .catch { exception ->
                isLoading = false
                viewModel.handleError(exception)
            }
            .collect { location ->
                isLoading = false
                location?.let { validLocation ->
                    viewModel.updateLocation(validLocation, chatId, chatType)
                    val position = CameraPosition.fromLatLngZoom(
                        LatLng(validLocation.latitude, validLocation.longitude),
                        15f
                    )
                    cameraPositionState.animate(CameraUpdateFactory.newCameraPosition(position))
                }
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(R.string.real_time_location)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.navigation_back)
                        )
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                properties = MapProperties(
                    isMyLocationEnabled = true,
                    mapType = MapType.NORMAL
                ),
                uiSettings = MapUiSettings(
                    zoomControlsEnabled = true,
                    myLocationButtonEnabled = true,
                    compassEnabled = true
                )
            )

            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .size(48.dp)
                        .align(Alignment.Center)
                )
            }

            uiState.error?.let { error ->
                Snackbar(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp)
                ) {
                    Text(text = error)
                }
            }
        }
    }

    val permissions = remember {
        mutableListOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ).apply {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                add(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
            }
        }
    }

    PermissionDialog(
        permissions = permissions,
        showDialog = showPermissionDialog,
        onPermissionResult = { granted ->
            if (!granted) {
                onNavigateBack()
            }
        }
    )
}
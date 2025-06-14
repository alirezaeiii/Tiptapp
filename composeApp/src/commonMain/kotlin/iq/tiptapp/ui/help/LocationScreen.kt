package iq.tiptapp.ui.help

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import component.CustomTopAppBar
import dev.icerock.moko.geo.compose.BindLocationTrackerEffect
import dev.icerock.moko.permissions.PermissionState
import dev.icerock.moko.permissions.compose.BindEffect
import dev.icerock.moko.permissions.compose.rememberPermissionsControllerFactory
import io.github.aakira.napier.Napier
import iq.tiptapp.Turquoise
import iq.tiptapp.location.getPlatformLocationProvider
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import tiptapp.composeapp.generated.resources.Res
import tiptapp.composeapp.generated.resources.current_location
import tiptapp.composeapp.generated.resources.location_permission_denied
import tiptapp.composeapp.generated.resources.location_permission_denied_permanent
import tiptapp.composeapp.generated.resources.open_app_setting

@Composable
fun LocationScreen(
    title: StringResource,
    onBackClicked: () -> Unit,
    setupLatLngToNavigate: (Double, Double) -> Unit
) {
    val factory = rememberPermissionsControllerFactory()
    val controller = remember(factory) {
        factory.createPermissionsController()
    }
    val locationTracker = getPlatformLocationProvider(controller)

    BindEffect(controller)
    BindLocationTrackerEffect(locationTracker)

    val viewModel = viewModel {
        LocationPermissionViewModel(controller, locationTracker)
    }

    val location = viewModel.latLng.collectAsState().value
    val address = viewModel.address.collectAsState().value
    val permissionState = viewModel.state
    val isLoading = viewModel.isLoading

    LaunchedEffect(permissionState) {
        if (permissionState == PermissionState.Granted) {
            viewModel.refreshLocation()
        }
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        Column(Modifier.fillMaxSize()) {
            CustomTopAppBar(title, onBackClicked)
            Column(
                modifier = Modifier
                    .fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                when (permissionState) {
                    PermissionState.Granted -> {
                        Napier.d("Permission granted.")
                    }

                    PermissionState.DeniedAlways -> {
                        Text(stringResource(Res.string.location_permission_denied_permanent))
                        Button(onClick = {
                            controller.openAppSettings()
                        }) {
                            Text(stringResource(Res.string.open_app_setting))
                        }
                    }

                    PermissionState.Denied -> {
                        Text(stringResource(Res.string.location_permission_denied))
                    }

                    else -> {
                        Napier.d("Requesting permission...")
                    }
                }

                address?.let {
                    Text(
                        stringResource(Res.string.current_location),
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 8.dp) // Optional for spacing
                            .align(Alignment.Start)
                    )
                    Box(
                        modifier = Modifier.fillMaxWidth()
                            .padding(16.dp)
                            .background(Color.White)
                            .border(
                                width = 1.dp,
                                color = Color.LightGray,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .padding(start = 12.dp, end = 12.dp, top = 24.dp, bottom = 24.dp)
                            .clickable {
                                location?.let { loc ->
                                    setupLatLngToNavigate.invoke(loc.latitude, loc.longitude)
                                }
                            }
                    ) {
                        Text(
                            text = it,
                            fontSize = 14.sp,
                            color = Color.DarkGray
                        )
                    }
                }
            }
        }
        if (isLoading) {
            CircularProgressIndicator(color = Turquoise)
        }
    }
}
@file:OptIn(ExperimentalPermissionsApi::class)

package com.chase.weather.view

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.annotation.SuppressLint
import android.content.Context
import android.view.KeyEvent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.chase.weather.R
import com.chase.weather.model.Location
import com.chase.weather.ui.theme.JPMCWeatherTheme
import com.chase.weather.viewmodel.LocationViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class) // for Scaffold, ElevatedCard, not crucial
@Composable
fun LocationScreen(navController: NavHostController) {
    val viewModel = remember { LocationViewModel() }
    var query: String by rememberSaveable { mutableStateOf("") }
    val focusManager = LocalFocusManager.current
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope: CoroutineScope = rememberCoroutineScope()
    val permissionState = rememberPermissionState(ACCESS_COARSE_LOCATION)
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(), onResult = { isGranted ->
            if (isGranted) {
                getDeviceLocation(context, navController, viewModel)
            }
        }
    )

    JPMCWeatherTheme {
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
        ) { _ ->
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                Column {
                    TopAppBar(
                        title = {
                            Text(LocalContext.current.resources.getString(R.string.screen_title_location))
                        },
                        colors = TopAppBarDefaults.smallTopAppBarColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                    )
                    OutlinedTextField(
                        enabled = !viewModel.isLoading.value,
                        value = query,
                        onValueChange = { query = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                            .onKeyEvent {
                                if (it.nativeKeyEvent.keyCode == KeyEvent.KEYCODE_ENTER) {
                                    viewModel.search(query)
                                    focusManager.clearFocus()
                                    true
                                }
                                false
                            },
                        singleLine = true,
                        label = { Text(text = LocalContext.current.resources.getString(R.string.location_search_hint)) },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Search,
                        ),
                        keyboardActions = KeyboardActions(
                            onSearch = {
                                viewModel.search(query)
                                focusManager.clearFocus()
                            },
                        ),
                        leadingIcon = {
                            Icon(
                                Icons.Filled.Search,
                                contentDescription = "Search Icon"
                            )
                        },
                        textStyle = LocalTextStyle.current.copy(fontSize = 24.sp)
                    )

                    if (!viewModel.isLoading.value) {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp)
                        ) {
                            items(viewModel.searchResults.value) { geocodeResponseItem ->
                                ElevatedCard(
                                    modifier = Modifier
                                        .fillMaxWidth(),
                                    colors = CardDefaults.elevatedCardColors(
                                        containerColor = MaterialTheme.colorScheme.background
                                    ),
                                    onClick = {
                                        viewModel.saveLocation(geocodeResponseItem)
                                        navController.navigate("weather")
                                    }) {
                                    Text(
                                        geocodeResponseItem.displayText(),
                                        fontSize = 24.sp,
                                        style = MaterialTheme.typography.displayLarge,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
                if (viewModel.isLoading.value) {
                    Box(modifier = Modifier.size(72.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                ButtonWithIcon(
                    modifier = Modifier
                        .padding(16.dp)
                        .wrapContentSize(Alignment.BottomEnd)
                        .padding(8.dp),
                    enabled = !viewModel.isLoading.value,
                    onClick = {
                        if (permissionState.status.isGranted) {
                            getDeviceLocation(context, navController, viewModel)
                        } else {
                            when {
                                permissionState.status.shouldShowRationale -> {
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar(
                                            context.getString(R.string.location_permission_rationale)
                                        )
                                    }
                                }

                                else -> {
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar(
                                            context.getString(R.string.location_permission_denied)
                                        )
                                    }
                                }
                            }
                            launcher.launch(ACCESS_COARSE_LOCATION)
                        }

                    },
                    text = LocalContext.current.resources.getString(R.string.use_gps_button),
                    imageVector = Icons.Default.LocationOn
                )
            }
        }
    }

}



@SuppressLint("MissingPermission")
fun getDeviceLocation(
    context: Context,
    navController: NavHostController,
    locationViewModel: LocationViewModel
) {
    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    fusedLocationClient.lastLocation
        .addOnSuccessListener { location: android.location.Location? ->
            if (location != null) {
                val location = Location(
                    lat = location.latitude,
                    lon = location.longitude
                )
                locationViewModel.saveLocation(location)
                navController.navigate("weather")
            }
        }
        .addOnFailureListener {
            // TODO this is just for debugging for now
            // Check under what conditions it actually happens and what we can do about it
            // Then decide on actual message text (move to strings) and display (dialog? snackbar?)
            Toast.makeText(context, "fusedLocationClient failed", Toast.LENGTH_SHORT).show()
        }

}


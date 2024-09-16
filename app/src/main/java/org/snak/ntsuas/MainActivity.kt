package org.snak.ntsuas

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.RequiresPermission
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import org.snak.ntsuas.ui.VarioViewModel
import org.snak.ntsuas.ui.theme.NtsuasTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContent {
            NtsuasTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Column {
                        val varioViewModel = this@MainActivity.varioViewModel
                        Altitude(
                            altitude = varioViewModel.altitude.collectAsState().value,
                            modifier = Modifier.padding(innerPadding)
                        )
                        SpinButton(title = "Reset", spinning = this@MainActivity.applyingAltitude) {
                            when {
                                ContextCompat.checkSelfPermission(
                                    this@MainActivity,
                                    Manifest.permission.ACCESS_FINE_LOCATION
                                ) == PackageManager.PERMISSION_GRANTED -> {
                                    this@MainActivity.applyCurrentAltitude()
                                }

                                ActivityCompat.shouldShowRequestPermissionRationale(
                                    this@MainActivity,
                                    Manifest.permission.ACCESS_FINE_LOCATION
                                ) -> {
                                    // TODO
                                    // Show a message
                                }

                                else -> {
                                    this@MainActivity.setAltitudeLauncher.launch(
                                        arrayOf(
                                            Manifest.permission.ACCESS_FINE_LOCATION,
                                            Manifest.permission.ACCESS_COARSE_LOCATION
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        this.fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
    }

    @RequiresPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    private suspend fun applyCurrentAltitude() {
        this.applyingAltitude.update { true }

        val location = this.fusedLocationClient.getCurrentLocation(
            Priority.PRIORITY_HIGH_ACCURACY,
            null
        ).await()
        this.varioViewModel.setAltitude(location.altitude)

        this.applyingAltitude.update { false }
    }

    private val varioViewModel: VarioViewModel by viewModels() {
        VarioViewModel.Factory
    }

    private var setAltitudeLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            if (permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false)) {
                this.lifecycleScope.launch {
                    this@MainActivity.applyCurrentAltitude()
                }
            } else {
                // TODO
                // Show a message
            }
        }
    private var applyingAltitude = MutableStateFlow(false)

    private lateinit var fusedLocationClient: FusedLocationProviderClient
}

@Composable
private fun SpinButton(title: String, spinning: StateFlow<Boolean>, job: suspend () -> Unit) {
    val scope = rememberCoroutineScope()

    Column {
        Button(
            onClick = {
                scope.launch {
                    job()
                }
            },
            enabled = !spinning.collectAsState().value
        ) {
            if (spinning.collectAsState().value) {
                CircularProgressIndicator()
            } else {
                Text(title)
            }
        }
    }
}

@Composable
fun Altitude(altitude: Double, modifier: Modifier = Modifier) {
    Text(
        text = "${altitude}",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun AltitudePreview() {
    NtsuasTheme {
        Altitude(432.1234567)
    }
}
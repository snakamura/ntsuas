package org.snak.ntsuas

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.icu.text.DecimalFormat
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
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
                    Vario(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }

        this.fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
    }

    @Composable
    private fun Vario(modifier: Modifier = Modifier) {
        var applyingCurrentAltitude by remember { mutableStateOf(false) }

        @RequiresPermission(Manifest.permission.ACCESS_FINE_LOCATION)
        suspend fun applyCurrentAltitude() {
            applyingCurrentAltitude = true

            val location = this.fusedLocationClient.getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY,
                null
            ).await()
            this.varioViewModel.setAltitude(location.altitude)

            applyingCurrentAltitude = false
        }

        val scope = rememberCoroutineScope()
        val setAltitudeLauncher = rememberLauncherForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            if (permissions.values.all { it }) {
                scope.launch {
                    applyCurrentAltitude()
                }
            }
        }

        // TODO
        // This should be determined by directly checking if the service is running
        var varioServiceRunning by remember { mutableStateOf(false) }

        val varioServiceIntent = Intent(this, VarioService::class.java)

        fun startVarioService() {
            ContextCompat.startForegroundService(this, varioServiceIntent)

            varioServiceRunning = true
        }

        fun stopVarioService() {
            this.stopService(varioServiceIntent)

            varioServiceRunning = false
        }

        val startVarioServiceLauncher = rememberLauncherForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            if (permissions.values.all { it }) {
                startVarioService()
            }
        }

        Column(
            modifier = modifier
        ) {
            val varioViewModel = this@MainActivity.varioViewModel
            Altitude(
                altitude = varioViewModel.altitude.collectAsState().value,
            )
            SpinButton(title = "Reset", spinning = applyingCurrentAltitude) {
                if (this@MainActivity.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    scope.launch {
                        applyCurrentAltitude()
                    }
                } else {
                    setAltitudeLauncher.launch(
                        arrayOf(
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        )
                    )
                }
            }
            Button(onClick = {
                if (!varioServiceRunning) {
                    if (this@MainActivity.checkSelfPermission(Manifest.permission.FOREGROUND_SERVICE_SPECIAL_USE) == PackageManager.PERMISSION_GRANTED) {
                        startVarioService()
                    } else {
                        startVarioServiceLauncher.launch(
                            arrayOf(
                                Manifest.permission.FOREGROUND_SERVICE,
                                Manifest.permission.FOREGROUND_SERVICE_SPECIAL_USE
                            )
                        )
                    }
                } else {
                    stopVarioService()
                }
            }) {
                Text(text = if (!varioServiceRunning) "Start" else "Stop")
            }
        }
    }

    private val varioViewModel: VarioViewModel by viewModels() {
        VarioViewModel.Factory
    }

    private lateinit var fusedLocationClient: FusedLocationProviderClient
}

@Composable
private fun SpinButton(title: String, spinning: Boolean, job: () -> Unit) {
    Column {
        Button(
            onClick = job,
            enabled = !spinning
        ) {
            if (spinning) {
                CircularProgressIndicator()
            } else {
                Text(title)
            }
        }
    }
}

@Composable
fun Altitude(altitude: Double, modifier: Modifier = Modifier) {
    val format = DecimalFormat("#,##0.00")
    Text(
        text = format.format(altitude),
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
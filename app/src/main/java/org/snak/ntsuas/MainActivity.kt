package org.snak.ntsuas

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.icu.text.DecimalFormat
import android.os.Build
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
            this.varioViewModel.setBaseAltitude(location.altitude)

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
            Decimal(
                value = varioViewModel.altitude.collectAsState().value,
                format = "#,###.00"
            )
            Decimal(
                value = varioViewModel.pressure.collectAsState().value,
                format = "#,###.0"
            )
            Decimal(
                value = varioViewModel.temperature.collectAsState().value,
                format = "#,###.0"
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
                        val permissions =
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                                arrayOf(
                                    Manifest.permission.FOREGROUND_SERVICE,
                                    Manifest.permission.FOREGROUND_SERVICE_SPECIAL_USE
                                )
                            } else {
                                arrayOf(
                                    Manifest.permission.FOREGROUND_SERVICE
                                )
                            }
                        startVarioServiceLauncher.launch(
                            permissions
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
fun Decimal(value: Double?, format: String, modifier: Modifier = Modifier) {
    val format = DecimalFormat(format)
    Text(
        text = if (value != null) format.format(value) else "-",
        modifier = modifier
    )
}

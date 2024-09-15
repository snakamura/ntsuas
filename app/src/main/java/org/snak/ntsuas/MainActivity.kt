package org.snak.ntsuas

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import org.snak.ntsuas.model.Vario
import org.snak.ntsuas.ui.theme.NtsuasTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NtsuasTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Column {
                        val vario = this@MainActivity._vario
                        Altitude(
                            altitude = vario.altitude.collectAsState().value,
                            modifier = Modifier.padding(innerPadding)
                        )
                        Button(onClick = {
                            this@MainActivity.lifecycleScope.launch {
                                vario.setAltitude(123.123456)
                            }
                        }) {
                            Text(text = "Set")
                        }
                    }
                }
            }
        }
    }

    private val _vario: Vario = Vario()
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
package org.snak.ntsuas.ui

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.StateFlow
import org.snak.ntsuas.model.Vario

class VarioViewModel : ViewModel() {
    val altitude: StateFlow<Double>
        get() = this.vario.altitude

    fun setAltitude(altitude: Double) {
        this.vario.setAltitude(altitude)
    }

    // TODO
    // Creating a Vario instance here isn't correct.
    // It shouldn't be created by UI.
    private val vario: Vario = Vario()
}

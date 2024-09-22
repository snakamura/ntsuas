package org.snak.ntsuas.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.flow.StateFlow
import org.snak.ntsuas.NtsuasApplication
import org.snak.ntsuas.model.Vario

class VarioViewModel(
    private val vario: Vario
) : ViewModel() {
    val altitude: StateFlow<Double?>
        get() = this.vario.altitude

    fun setBaseAltitude(altitude: Double) {
        this.vario.setBaseAltitude(altitude)
    }

    val pressure: StateFlow<Double?>
        get() = this.vario.pressure

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            this.initializer {
                val vario = (this[APPLICATION_KEY] as NtsuasApplication).vario
                VarioViewModel(vario)
            }
        }
    }
}

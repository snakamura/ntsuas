package org.snak.ntsuas.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
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
        // We should use mapState described in
        // https://github.com/Kotlin/kotlinx.coroutines/issues/2631
        get() = this.vario.pressure.map { it?.value }
            .stateIn(this.viewModelScope, SharingStarted.Eagerly, this.vario.pressure.value?.value)

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            this.initializer {
                val vario = (this[APPLICATION_KEY] as NtsuasApplication).vario
                VarioViewModel(vario)
            }
        }
    }
}

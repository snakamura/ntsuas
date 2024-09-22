package org.snak.ntsuas.model

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class Vario {
    private val _altitude: MutableStateFlow<Double?> = MutableStateFlow(null)
    val altitude: StateFlow<Double?> = this._altitude.asStateFlow()

    fun setAltitude(altitude: Double) {
        this._altitude.update { altitude }
    }

    private val _pressure: MutableStateFlow<Double?> = MutableStateFlow(null)
    val pressure: StateFlow<Double?> = this._pressure.asStateFlow()

    fun setPressure(pressure: Double) {
        this._pressure.update { pressure }
    }
}

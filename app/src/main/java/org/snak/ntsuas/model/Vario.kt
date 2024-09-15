package org.snak.ntsuas.model

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class Vario {
    val altitude: StateFlow<Double>
        get() = this._altitude

    suspend fun setAltitude(altitude: Double) {
        this._altitude.emit(altitude)
    }

    private val _altitude: MutableStateFlow<Double> = MutableStateFlow(0.0)
}

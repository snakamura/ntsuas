package org.snak.ntsuas.model

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class Vario {
    private val _altitude: MutableStateFlow<Double> = MutableStateFlow(0.0)
    val altitude: StateFlow<Double> = this._altitude.asStateFlow()

    suspend fun setAltitude(altitude: Double) {
        this._altitude.emit(altitude)
    }
}

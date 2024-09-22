package org.snak.ntsuas.model

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlin.math.pow

class Vario {
    private val _altitude: MutableStateFlow<Double?> = MutableStateFlow(null)
    val altitude: StateFlow<Double?> = this._altitude.asStateFlow()

    private var baseAltitude: Double? = null

    fun setBaseAltitude(altitude: Double) {
        this.baseAltitude = altitude
        this.basePressure = this.pressure.value

        this._altitude.update { altitude }
    }

    private val _pressure: MutableStateFlow<Double?> = MutableStateFlow(null)
    val pressure: StateFlow<Double?> = this._pressure.asStateFlow()

    fun setPressure(pressure: Double) {
        if (this.basePressure == null) {
            this.basePressure = pressure
        }

        this._pressure.update { pressure }

        this.updateAltitude()
    }

    private var basePressure: Double? = null

    private fun updateAltitude() {
        val baseAltitude = this.baseAltitude ?: return
        val basePressure = this.basePressure ?: return
        val baseTemperature = this.baseTemperature ?: return
        val pressure = this.pressure.value ?: return

        val altitude =
            baseAltitude - (baseTemperature * (pressure / basePressure).pow(gamma * R / g) - baseTemperature) / gamma

        this._altitude.update { altitude }
    }

    private var baseTemperature: Double? = 25.0 + 273.15

    companion object {
        private const val gamma = 6.5 / 1000
        private const val R = 287
        private const val g = 9.81
    }
}

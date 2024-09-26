package org.snak.ntsuas.model

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlin.math.pow

data class Pressure(
    val value: Double,
    val timestamp: Long
)

class Vario {
    private val _altitude: MutableStateFlow<Double?> = MutableStateFlow(null)
    val altitude: StateFlow<Double?> = this._altitude.asStateFlow()

    private var baseAltitude: Double? = null

    fun setBaseAltitude(altitude: Double) {
        this.baseAltitude = altitude
        this.basePressure = this.pressure.value?.value

        this._altitude.update { altitude }
    }

    private val _pressure: MutableStateFlow<Pressure?> = MutableStateFlow(null)
    val pressure: StateFlow<Pressure?> = this._pressure.asStateFlow()

    fun setPressure(pressure: Double, timestamp: Long) {
        if (this.baseAltitude != null && this.basePressure == null) {
            this.basePressure = pressure
        }

        this._pressure.update { Pressure(pressure, timestamp) }

        this.updateAltitude()
    }

    private var basePressure: Double? = null

    private val _temperature: MutableStateFlow<Double?> = MutableStateFlow(null)
    val temperature: StateFlow<Double?> = this._temperature.asStateFlow()

    fun setTemperature(temperature: Double) {
        if (this.baseAltitude != null && this.baseTemperature == null) {
            this.setBaseTemperature(temperature)
        }

        this._temperature.update { temperature }
    }

    fun setBaseTemperature(temperature: Double) {
        this.baseTemperature = celsiusToKelvin(temperature)
        this._temperature.update { temperature }
        this.updateAltitude()
    }

    private var baseTemperature: Double? = null

    private fun updateAltitude() {
        val baseAltitude = this.baseAltitude ?: return
        val basePressure = this.basePressure ?: return
        val baseTemperature = this.baseTemperature ?: return
        val pressure = this.pressure.value ?: return

        val altitude =
            baseAltitude - (baseTemperature * (pressure.value / basePressure).pow(gamma * R / g) - baseTemperature) / gamma

        this._altitude.update { altitude }
    }

    companion object {
        private fun celsiusToKelvin(celsius: Double): Double {
            return celsius + 273.15
        }

        private const val gamma = 6.5 / 1000
        private const val R = 287
        private const val g = 9.81
    }
}

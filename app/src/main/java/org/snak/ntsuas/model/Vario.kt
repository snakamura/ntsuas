package org.snak.ntsuas.model

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

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

    private fun updateAltitude() {
        val baseAltitude = this.baseAltitude ?: return
        val basePressure = this.basePressure ?: return
        val pressure = this.pressure.value ?: return

        val filter = this.filter
        if (filter == null) {
            this.filter =
                VarioFilter(
                    baseAltitude,
                    INITIAL_ALTITUDE_VARIANCE,
                    baseAltitude,
                    basePressure,
                    PROCESS_NOISE_VARIANCE
                )
        } else {
            this._altitude.update {
                filter.estimate(pressure.value, ALTITUDE_VARIANCE)
            }
        }
    }

    private var filter: VarioFilter? = null

    companion object {
        private fun celsiusToKelvin(celsius: Double): Double {
            return celsius + 273.15
        }

        private const val TEMPERATURE_AT_SEA_LEVEL = 15.0

        private const val GAMMA = 6.5 / 1000
        private const val R = 287
        private const val G = 9.81

        private const val INITIAL_ALTITUDE_VARIANCE = 0.0
        private const val ALTITUDE_VARIANCE = 0.1
        private const val PROCESS_NOISE_VARIANCE = 0.1
    }
}

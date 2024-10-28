package org.snak.ntsuas.model

import kotlin.math.pow

class VarioFilter(
    initialAltitude: Double,
    initialVariance: Double,
    private val baseAltitude: Double,
    private val basePressure: Double,
    private val processNoiseVariance: Double
) {
    fun estimate(pressure: Double, variance: Double): Double {
        val predicatedAltitude = this.currentAltitude
        val predicatedVariance = this.currentVariance + this.processNoiseVariance

        val baseTemperature = TEMPERATURE_AT_SEA_LEVEL - baseAltitude * GAMMA
        fun altitudeToPressure(altitude: Double): Double =
            this.basePressure * ((baseTemperature - GAMMA * (altitude - baseAltitude)) / baseTemperature).pow(
                G / (GAMMA * R)
            )

        fun altitudeToPressureDiff(altitude: Double): Double =
            -basePressure * G * ((baseTemperature - GAMMA * (altitude - baseAltitude)) / baseTemperature).pow(
                (G / (GAMMA * R)) / (R * (baseTemperature - GAMMA * (altitude - baseAltitude)))
            )

        val observedPressure = altitudeToPressure(this.currentAltitude)
        var jacobian = altitudeToPressureDiff(this.currentAltitude)

        val gain =
            (predicatedVariance * jacobian) / (jacobian * predicatedVariance * jacobian + variance)

        val estimatedAltitude = predicatedAltitude + gain * (pressure - observedPressure)
        val estimatedVariance =
            (1 - gain * jacobian) * predicatedVariance * (1 - gain * jacobian) + gain * variance * gain

        this.currentAltitude = estimatedAltitude
        this.currentVariance = estimatedVariance

        return estimatedAltitude
    }

    private var currentAltitude = initialAltitude
    private var currentVariance = initialVariance

    companion object {
        private const val TEMPERATURE_AT_SEA_LEVEL = 15.0

        private const val GAMMA = 6.5 / 1000
        private const val R = 287
        private const val G = 9.81
    }
}

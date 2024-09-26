package org.snak.ntsuas.model

class VarioFilter(
    initialAltitude: Double,
    initialVariance: Double,
    private val processNoiseVariance: Double
) {
    fun estimate(altitude: Double, variance: Double): Double {
        val predicatedAltitude = this.currentAltitude
        val predicatedVariance = this.currentVariance + this.processNoiseVariance
        val gain = predicatedVariance / (predicatedVariance + variance)

        val estimatedAltitude = predicatedAltitude + gain * (altitude - predicatedAltitude)
        val estimatedVariance = (1 - gain) * predicatedVariance

        this.currentAltitude = estimatedAltitude
        this.currentVariance = estimatedVariance

        return estimatedAltitude
    }

    private var currentAltitude = initialAltitude
    private var currentVariance = initialVariance
}

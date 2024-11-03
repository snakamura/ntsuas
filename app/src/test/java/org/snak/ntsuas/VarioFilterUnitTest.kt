package org.snak.ntsuas

import org.junit.Assert
import org.junit.Test

import org.snak.ntsuas.model.VarioFilter

class VarioFilterUnitTest {
    @Test
    fun estimate() {
        val filter = VarioFilter(
            initialAltitude = 500.0,
            initialVariance = 10000.0,
            baseAltitude = 0.0,
            basePressure = 1013.0,
            processNoiseVariance = 10.0
        )

        val items = listOf(
            Pair(950.0, 537.64298),
            Pair(950.2, 536.90680),
            Pair(950.4, 535.94960),
            Pair(950.6, 534.84237),
            Pair(950.8, 533.60068),
            Pair(951.0, 532.24108),
            Pair(951.2, 530.78343),
            Pair(951.4, 529.24801),
            Pair(951.6, 527.65311),
            Pair(951.8, 526.01394),
        )
        for ((pressure, altitude) in items) {
            val estimatedAltitude = filter.estimate(
                pressure = pressure,
                variance = 1.0
            )
            Assert.assertEquals(altitude, estimatedAltitude, DELTA)
        }
    }

    @Test
    fun altitudeToPressure() {
        val filter = VarioFilter(
            initialAltitude = 500.0,
            initialVariance = 100.0,
            baseAltitude = 0.0,
            basePressure = 1023.0,
            processNoiseVariance = 10.0
        )

        val pressureAt0 = filter.altitudeToPressure(0.0)
        Assert.assertEquals(1023.0, pressureAt0, DELTA)

        val pressureAt1000 = filter.altitudeToPressure(1000.0)
        Assert.assertEquals(907.33659, pressureAt1000, DELTA)

        val pressureAt1500 = filter.altitudeToPressure(1500.0)
        Assert.assertEquals(853.61512, pressureAt1500, DELTA)

        val pressureAt2000 = filter.altitudeToPressure(2000.0)
        Assert.assertEquals(802.49904, pressureAt2000, DELTA)
    }

    companion object {
        private const val DELTA = 0.0001
    }
}
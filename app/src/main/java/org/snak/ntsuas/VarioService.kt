package org.snak.ntsuas

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import org.snak.ntsuas.model.Vario

class VarioService : Service() {
    override fun onBind(intent: Intent?): IBinder? {
        throw UnsupportedOperationException()
    }

    override fun onCreate() {
        super.onCreate()

        this.vario = (this.application as NtsuasApplication).vario
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val sensorManager = this.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val pressureSensor = sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE)
        if (pressureSensor != null) {
            if (sensorManager.registerListener(
                    this.sensorEventListener,
                    pressureSensor,
                    SensorManager.SENSOR_DELAY_FASTEST
                )
            ) {
                this.sensorManager = sensorManager
            }
        }

        val notificationChannel =
            NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT)
        val notificationManager = this.getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(notificationChannel)

        val notification = NotificationCompat.Builder(this, CHANNEL_ID).apply {
            this.setContentTitle("Vario")
        }.build()

        ServiceCompat.startForeground(
            this,
            ID,
            notification,
            ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
        )

        return START_STICKY
    }

    override fun onDestroy() {
        val sensorManager = this.sensorManager
        if (sensorManager != null) {
            sensorManager.unregisterListener(this.sensorEventListener)
            this.sensorManager = null
        }

        super.onDestroy()
    }

    private lateinit var vario: Vario

    private var sensorManager: SensorManager? = null

    private val sensorEventListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent?) {
            if (event != null && event.values.size > 0) {
                this@VarioService.vario.setPressure(event.values[0].toDouble())
            }
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        }
    }


    companion object {
        private const val CHANNEL_ID = "Vario"
        private const val CHANNEL_NAME = "Vario"
        private const val ID = 1
    }
}
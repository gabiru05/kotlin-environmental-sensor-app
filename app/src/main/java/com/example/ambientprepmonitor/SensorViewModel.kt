package com.example.ambientprepmonitor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import kotlin.math.sqrt

// --- ViewModel ---
class SensorViewModel(private val sensorManager: SensorManager) : ViewModel(), SensorEventListener {

    // Sensores
    private var tempSensor: Sensor? = null
    private var gyroSensor: Sensor? = null
    private var pressureSensor: Sensor? = null

    // Estados para los valores de los sensores
    var ambientTemperature by mutableStateOf<Float?>(null)
        private set
    var gyroscopeValues by mutableStateOf<FloatArray?>(null) // x, y, z: rad/s
        private set
    var pressureValue by mutableStateOf<Float?>(null) // hPa
        private set
    var deviceStability by mutableStateOf<DeviceStability>(DeviceStability.UNKNOWN)
        private set

    // Estados para la disponibilidad de los sensores
    var tempSensorAvailable by mutableStateOf(false)
        private set
    var gyroSensorAvailable by mutableStateOf(false)
        private set
    var pressureSensorAvailable by mutableStateOf(false)
        private set

    // Umbral para considerar el dispositivo inestable (magnitud de velocidad angular en rad/s)
    private val GYRO_STABILITY_THRESHOLD = 0.3f // Reducido para mayor sensibilidad

    enum class DeviceStability(val displayText: String, val color: Color) {
        STABLE("Estable", Color(0xFF4CAF50)), // Verde (Material Green 500)
        UNSTABLE("Inestable", Color(0xFFF44336)), // Rojo (Material Red 500)
        UNKNOWN("Analizando...", Color.Gray)
    }

    init {
        // Inicializar sensor de temperatura ambiente
        tempSensor = sensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE)
        if (tempSensor != null) {
            tempSensorAvailable = true
            sensorManager.registerListener(this, tempSensor, SensorManager.SENSOR_DELAY_UI)
        } else {
            tempSensorAvailable = false
        }

        // Inicializar giroscopio
        gyroSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
        if (gyroSensor != null) {
            gyroSensorAvailable = true
            sensorManager.registerListener(this, gyroSensor, SensorManager.SENSOR_DELAY_UI)
        } else {
            gyroSensorAvailable = false
            deviceStability = DeviceStability.UNKNOWN // Si no hay giroscopio, no se puede determinar
        }

        // Inicializar sensor de presión (barómetro)
        pressureSensor = sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE)
        if (pressureSensor != null) {
            pressureSensorAvailable = true
            sensorManager.registerListener(this, pressureSensor, SensorManager.SENSOR_DELAY_UI)
        } else {
            pressureSensorAvailable = false
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            when (it.sensor.type) {
                Sensor.TYPE_AMBIENT_TEMPERATURE -> {
                    ambientTemperature = it.values[0]
                }
                Sensor.TYPE_GYROSCOPE -> {
                    gyroscopeValues = it.values.clone() // Clonar para evitar problemas de referencia
                    // Calcular la magnitud de la velocidad angular
                    val x = it.values[0]
                    val y = it.values[1]
                    val z = it.values[2]
                    // Magnitud de la velocidad angular
                    val magnitude = sqrt(x * x + y * y + z * z)

                    deviceStability = if (magnitude < GYRO_STABILITY_THRESHOLD) {
                        DeviceStability.STABLE
                    } else {
                        DeviceStability.UNSTABLE
                    }
                }
                Sensor.TYPE_PRESSURE -> {
                    pressureValue = it.values[0]
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
 }

    // Es importante anular el registro de los listeners para ahorrar batería
    override fun onCleared() {
        super.onCleared()
        sensorManager.unregisterListener(this)
    }
}


class SensorViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SensorViewModel::class.java)) {
            val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
            @Suppress("UNCHECKED_CAST")
            return SensorViewModel(sensorManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class for SensorViewModelFactory")
    }
}


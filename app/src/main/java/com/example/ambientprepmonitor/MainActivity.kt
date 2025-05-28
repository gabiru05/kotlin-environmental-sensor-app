package com.example.ambientprepmonitor

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ambientprepmonitor.ui.theme.AmbientPrepMonitorTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AmbientPrepMonitorTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    // Inicializar ViewModel
                    val context = LocalContext.current
                    val sensorViewModel: SensorViewModel = viewModel(
                        factory = SensorViewModelFactory(context.applicationContext)
                    )
                    // Llama a tu pantalla principal de la app de sensores
                    AmbientPrepScreen(
                        viewModel = sensorViewModel,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun AmbientPrepScreen(viewModel: SensorViewModel, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Monitor de Preparación Ambiental",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "Verifica las condiciones ambientales y la estabilidad del dispositivo antes de tu actividad.",
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Spacer(modifier = Modifier.height(24.dp))

        SensorInfoCard(
            title = "Temperatura Ambiente",
            value = viewModel.ambientTemperature?.let { "%.1f °C".format(it) } ?: "N/A",
            isAvailable = viewModel.tempSensorAvailable
        )

        SensorInfoCard(
            title = "Estabilidad del Dispositivo",
            value = viewModel.deviceStability.displayText,
            valueColor = viewModel.deviceStability.color,
            isAvailable = viewModel.gyroSensorAvailable,
            extraInfo = viewModel.gyroscopeValues?.let {
                "Giroscopio:\nX: %.2f\nY: %.2f\nZ: %.2f rad/s".format(it[0], it[1], it[2])
            }
        )

        SensorInfoCard(
            title = "Presión Barométrica",
            value = viewModel.pressureValue?.let { "%.2f hPa".format(it) } ?: "N/A",
            isAvailable = viewModel.pressureSensorAvailable
        )
    }
}

@Composable
fun SensorInfoCard(
    title: String,
    value: String,
    isAvailable: Boolean,
    valueColor: Color = MaterialTheme.colorScheme.onSurface,
    extraInfo: String? = null
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant) // Usa colores del tema
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = title,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            if (isAvailable) {
                Text(
                    text = value,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = valueColor
                )
                extraInfo?.let {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = it,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                Text(
                    "Sensor no disponible",
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AmbientPrepScreenPreview() {
     AmbientPrepMonitorTheme {

        Surface(modifier = Modifier.padding(0.dp)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Monitor de Preparación Ambiental", fontSize = 24.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Verifica las condiciones ambientales...", fontSize = 14.sp, textAlign = TextAlign.Center, modifier = Modifier.padding(horizontal = 16.dp))
                Spacer(modifier = Modifier.height(24.dp))

                SensorInfoCard(title = "Temperatura Ambiente", value = "25.0 °C", isAvailable = true)
                SensorInfoCard(title = "Estabilidad del Dispositivo", value = "Estable", valueColor = Color(0xFF4CAF50), isAvailable = true, extraInfo = "Giroscopio:\nX: 0.01\nY: 0.02\nZ: 0.00 rad/s")
                SensorInfoCard(title = "Presión Barométrica", value = "1012.50 hPa", isAvailable = true)
                SensorInfoCard(title = "Sensor Faltante", value = "N/A", isAvailable = false)
            }
        }
    }
}

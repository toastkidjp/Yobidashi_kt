package jp.toastkid.sensor.presentation.view.tab

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import jp.toastkid.sensor.application.model.Destination

@Composable
fun SensorView(destination: Destination) {
    val context = LocalContext.current

    val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    val lightSensor = remember {
        mutableStateOf<Sensor?>(null)
    }

    val text = remember { mutableStateOf("-") }

    val sensorListener = remember {
        object : SensorEventListener {

            override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
                //TODO("Not yet implemented")
            }

            override fun onSensorChanged(sensorEvent: SensorEvent?) {
                text.value = "${sensorEvent?.values[0]}${destination.unit}"
            }

        }
    }

    val observer = remember {
        LifecycleEventObserver { source, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> {
                    if (lightSensor.value != null) {
                        sensorManager?.registerListener(
                            sensorListener,
                            lightSensor.value,
                            SensorManager.SENSOR_DELAY_NORMAL
                        )
                    }
                }

                Lifecycle.Event.ON_PAUSE -> {
                    sensorManager?.unregisterListener(sensorListener)
                }

                else -> Unit
            }
        }
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(destination) {
        sensorManager?.unregisterListener(sensorListener)
        lifecycleOwner.lifecycle.removeObserver(observer)
        lightSensor.value = sensorManager?.getDefaultSensor(destination.sensor)
        lifecycleOwner.lifecycle.addObserver(observer)

        if (sensorManager == null) {
            text.value = "SensorManager is not available."
        }

        if (lightSensor.value == null) {
            text.value = "Sensor is not available."
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Text(
            text.value,
            fontSize = 96.sp,
            lineHeight = 100.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.align(Alignment.Center)
        )
    }
}

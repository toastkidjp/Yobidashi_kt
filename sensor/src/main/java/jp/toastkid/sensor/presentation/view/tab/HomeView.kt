package jp.toastkid.sensor.presentation.view.tab

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorManager
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun HomeView() {
    Column(
        modifier = Modifier.padding(8.dp).verticalScroll(rememberScrollState())
    ) {
        Text(
            "This app provides functions which use Android device's sensor.",
            fontSize = 20.sp,
        )

        val context = LocalContext.current

        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
        if (sensorManager == null) {
            Text(
                "This app provides functions which use Android device's sensor.",
                fontSize = 20.sp,
                modifier = Modifier.padding(top = 16.dp)
            )
            return
        }

        val sensors: MutableList<Sensor?>? = sensorManager.getSensorList(Sensor.TYPE_ALL)
        sensors?.mapNotNull { it?.name }?.forEach { name ->
            Text(
                name,
                fontSize = 16.sp,
                modifier = Modifier.padding(8.dp)
            )
            HorizontalDivider(
                modifier = Modifier.padding(start = 16.dp)
            )
        }
    }
}
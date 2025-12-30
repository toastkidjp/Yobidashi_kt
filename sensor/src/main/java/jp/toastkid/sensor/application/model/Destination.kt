package jp.toastkid.sensor.application.model

import android.hardware.Sensor
import jp.toastkid.sensor.R

enum class Destination(
    val label: String,
    val icon: Int,
    val sensor: Int,
    val unit: String = ""
) {
    HOME("Home", R.drawable.ic_home, -1),
    AMBIENT_TEMPERATURE("Ambient Temperature", R.drawable.ic_lux, Sensor.TYPE_AMBIENT_TEMPERATURE, "℃"),
    LIGHT("Light", R.drawable.ic_lux, Sensor.TYPE_LIGHT, "lx"),
    GYROSCOPE("GYROSCOPE", R.drawable.ic_lux, Sensor.TYPE_GYROSCOPE, "°/h");

}

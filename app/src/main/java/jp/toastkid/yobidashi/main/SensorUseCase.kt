/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.main

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import java.util.Calendar

/**
 * @author toastkidjp
 */
class SensorUseCase(private val sensorManager: SensorManager) {

    private var sensorText = ""

    private val listeners = mapOf(
            Sensor.TYPE_LIGHT to makeListener("LIGHT"),
            Sensor.TYPE_RELATIVE_HUMIDITY to makeListener("HUMIDITY"),
            Sensor.TYPE_AMBIENT_TEMPERATURE to makeListener("AMBIENT_TEMPERATURE")
    )

    fun onResume() {
        listeners.forEach { registerListener(it.key, it.value) }
    }

    private fun makeListener(listenerName: String) =
            object : SensorEventListener {
                override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit

                override fun onSensorChanged(event: SensorEvent?) {
                    sensorText += LINE_SEPARATOR +
                            "$listenerName ${toTimeString(event?.timestamp)} ${event?.values?.get(0)}"
                }
            }

    private fun registerListener(listenerType: Int, eventListener: SensorEventListener) {
        sensorManager.getSensorList(listenerType)?.let {
            if (it.isNotEmpty()) {
                sensorManager.registerListener(
                        eventListener,
                        it.get(0),
                        SensorManager.SENSOR_DELAY_NORMAL
                )
            }
        }
    }

    private fun toTimeString(timestamp: Long?) =
            Calendar.getInstance().also { cal ->
                cal.timeInMillis = ((timestamp ?: 0) / 1000).takeIf { it != 0L } ?: cal.timeInMillis
            }.time

    fun onPause() {
        listeners.forEach { sensorManager.unregisterListener(it.value) }
    }

    fun getText() = sensorText

    companion object {
        private val LINE_SEPARATOR = System.lineSeparator()
    }

}
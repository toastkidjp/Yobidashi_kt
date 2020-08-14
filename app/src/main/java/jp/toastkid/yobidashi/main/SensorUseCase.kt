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

    private var humidityListener: SensorEventListener? = null
    private var tempListener: SensorEventListener? = null
    private var listener: SensorEventListener? = null

    private var sensorText = ""

    fun onResume() {
        sensorManager.getSensorList(Sensor.TYPE_RELATIVE_HUMIDITY)?.let {
            if (it.isNotEmpty()) {
                humidityListener = object : SensorEventListener {
                    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit

                    override fun onSensorChanged(event: SensorEvent?) {
                        sensorText += LINE_SEPARATOR +
                                "RELATIVE_HUMIDITY ${toTimeString(event?.timestamp)} ${event?.values?.get(0)}"
                    }
                }
                sensorManager.registerListener(
                        humidityListener,
                        it.get(0),
                        SensorManager.SENSOR_DELAY_NORMAL
                )
            }
        }
        sensorManager.getSensorList(Sensor.TYPE_LIGHT)?.let {
            if (it.isNotEmpty()) {
                listener = object : SensorEventListener {
                    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit

                    override fun onSensorChanged(event: SensorEvent?) {
                        sensorText += LINE_SEPARATOR + "LIGHT ${toTimeString(event?.timestamp)} ${event?.values?.get(0)}"
                    }
                }
                sensorManager.registerListener(
                        listener,
                        it.get(0),
                        SensorManager.SENSOR_DELAY_NORMAL
                )
            }
        }
        sensorManager.getSensorList(Sensor.TYPE_AMBIENT_TEMPERATURE)?.let {
            if (it.isNotEmpty()) {
                tempListener = object : SensorEventListener {
                    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit

                    override fun onSensorChanged(event: SensorEvent?) {
                        sensorText += LINE_SEPARATOR + "AMBIENT_TEMPERATURE ${toTimeString(event?.timestamp)} ${event?.values?.get(0)}"
                    }
                }
                sensorManager.registerListener(
                        tempListener,
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
        sensorManager.unregisterListener(humidityListener)
        sensorManager.unregisterListener(tempListener)
        sensorManager.unregisterListener(listener)
    }

    fun getText() = sensorText

    companion object {
        private val LINE_SEPARATOR = System.lineSeparator()
    }

}
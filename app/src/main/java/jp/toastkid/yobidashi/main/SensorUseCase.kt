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
                    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {

                    }

                    override fun onSensorChanged(event: SensorEvent?) {
                        sensorText += System.lineSeparator() +
                                "RELATIVE_HUMIDITY ${Calendar.getInstance().also { it.timeInMillis = event?.timestamp ?: it.timeInMillis }.time}${event?.values?.get(0)}"
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
                    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {

                    }

                    override fun onSensorChanged(event: SensorEvent?) {
                        sensorText += System.lineSeparator() + "LIGHT ${Calendar.getInstance().also { it.timeInMillis = event?.timestamp ?: it.timeInMillis }.time}${event?.values?.get(0)}"
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
                    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {

                    }

                    override fun onSensorChanged(event: SensorEvent?) {
                        sensorText += System.lineSeparator() + "AMBIENT_TEMPERATURE ${Calendar.getInstance().also { it.timeInMillis = event?.timestamp ?: it.timeInMillis }.time}${event?.values?.get(0)}"
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

    fun onPause() {
        sensorManager.unregisterListener(humidityListener)
        sensorManager.unregisterListener(tempListener)
        sensorManager.unregisterListener(listener)
    }

    fun getText(): String {
        return sensorText
    }
}
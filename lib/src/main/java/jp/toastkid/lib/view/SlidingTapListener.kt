/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.lib.view

import android.view.MotionEvent
import android.view.View
import kotlin.math.abs

/**
 * @author toastkidjp
 */
class SlidingTapListener(private val targetView: View) : View.OnTouchListener {

    private var downRawY = 0f

    private var dY = 0f

    private var onNewPosition: OnNewPosition? = null

    interface OnNewPosition {
        fun onNewPosition(x: Float, y: Float)
    }

    fun setCallback(callback: OnNewPosition) {
        onNewPosition = callback
    }

    override fun onTouch(view: View?, motionEvent: MotionEvent?): Boolean {
        if (view == null || motionEvent == null) {
            return false
        }

        return when (motionEvent.action) {
            MotionEvent.ACTION_DOWN -> {
                downRawY = motionEvent.rawY
                dY = targetView.y - downRawY
                true
            }
            MotionEvent.ACTION_MOVE -> {
                onNewPosition?.onNewPosition(targetView.x, motionEvent.rawY)
                return true
            }
            MotionEvent.ACTION_UP -> {
                val upRawY = motionEvent.rawY

                val upDY = upRawY - downRawY

                if (abs(upDY) < CLICK_DRAG_TOLERANCE) {
                    return view.performClick()
                }
                return true
            }
            else -> false
        }
    }

    companion object {
        private const val CLICK_DRAG_TOLERANCE = 10
    }

}
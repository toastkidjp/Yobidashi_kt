/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.browser.floating

import android.view.MotionEvent
import android.view.View

/**
 * @author toastkidjp
 */
class SlidingTouchListener(private val targetView: View) : View.OnTouchListener {

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
                val viewHeight = view.height

                val viewParent = view.parent as View
                val parentHeight = viewParent.height.toFloat()

                var newY = motionEvent.rawY + dY
                newY = Math.max(0f, newY)
                newY = Math.min(parentHeight - viewHeight, newY)

                targetView.animate()
                        .y(newY)
                        .setDuration(0)
                        .start()
                return true
            }
            MotionEvent.ACTION_UP -> {
                val upRawY = motionEvent.rawY

                val upDY = upRawY - downRawY

                if (Math.abs(upDY) < CLICK_DRAG_TOLERANCE) {
                    return view.performClick()
                }
                onNewPosition?.onNewPosition(view.x, view.y)
                return true
            }
            else -> false
        }
    }

    companion object {
        private const val CLICK_DRAG_TOLERANCE = 10
    }

}
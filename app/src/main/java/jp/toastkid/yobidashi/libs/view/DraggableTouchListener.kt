/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.libs.view

import android.view.MotionEvent
import android.view.View

/**
 * @author toastkidjp
 */
class DraggableTouchListener : View.OnTouchListener {

    private var downRawX = 0f

    private var downRawY = 0f

    private var dX = 0f

    private var dY = 0f

    override fun onTouch(view: View?, motionEvent: MotionEvent?): Boolean {
        if (view == null || motionEvent == null) {
            return false
        }

        return when (motionEvent.action) {
            MotionEvent.ACTION_DOWN -> {
                downRawX = motionEvent.rawX
                downRawY = motionEvent.rawY
                dX = view.x - downRawX
                dY = view.y - downRawY
                true
            }
            MotionEvent.ACTION_MOVE -> {
                val viewWidth = view.width
                val viewHeight = view.height

                val viewParent = view.parent as View
                val parentWidth = viewParent.width.toFloat()
                val parentHeight = viewParent.height.toFloat()

                var newX = motionEvent.rawX + dX
                newX = Math.max(0f, newX)
                newX = Math.min(parentWidth - viewWidth, newX)

                var newY = motionEvent.rawY + dY
                newY = Math.max(0f, newY)
                newY = Math.min(parentHeight - viewHeight, newY)

                view.animate()
                        .x(newX)
                        .y(newY)
                        .setDuration(0)
                        .start()
                return true
            }
            MotionEvent.ACTION_UP -> {
                val upRawX = motionEvent.rawX
                val upRawY = motionEvent.rawY

                val upDX = upRawX - downRawX
                val upDY = upRawY - downRawY

                if (Math.abs(upDX) < CLICK_DRAG_TOLERANCE && Math.abs(upDY) < CLICK_DRAG_TOLERANCE) {
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
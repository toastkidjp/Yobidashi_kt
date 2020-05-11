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

    private var onNewPosition: OnNewPosition? = null

    private var onClick: OnClick? = null

    interface OnNewPosition {
        fun onNewPosition(x: Float, y: Float)
    }

    interface OnClick {
        fun onClick()
    }

    fun setCallback(callback: OnNewPosition) {
        onNewPosition = callback
    }

    fun setOnClick(callback: OnClick) {
        onClick = callback
    }

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
                false
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
                false
            }
            MotionEvent.ACTION_UP -> {
                val upRawX = motionEvent.rawX
                val upRawY = motionEvent.rawY

                val upDX = upRawX - downRawX
                val upDY = upRawY - downRawY

                if (Math.abs(upDX) < CLICK_DRAG_TOLERANCE && Math.abs(upDY) < CLICK_DRAG_TOLERANCE) {
                    onClick?.onClick()
                    return false
                }
                onNewPosition?.onNewPosition(view.x, view.y)
                false
            }
            else -> false
        }
    }

    companion object {
        private const val CLICK_DRAG_TOLERANCE = 10
    }
}
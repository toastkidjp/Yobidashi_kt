/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.lib.view

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.annotation.Keep
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.ViewCompat
import androidx.core.view.isGone
import androidx.core.view.isVisible
import com.google.android.material.floatingactionbutton.FloatingActionButton

/**
 * @author toastkidjp
 */
@Keep
class HidingFloatingActionButtonBehavior<V : View>(context: Context, attrs: AttributeSet) :
        CoordinatorLayout.Behavior<V>(context, attrs) {

    private var lastY = 0f

    override fun onInterceptTouchEvent(
            parent: CoordinatorLayout,
            child: V,
            motionEvent: MotionEvent
    ): Boolean {
        if (motionEvent.action == MotionEvent.ACTION_DOWN) {
            lastY = motionEvent.rawY
        }

        if (child !is FloatingActionButton || child.isVisible) {
            return super.onInterceptTouchEvent(parent, child, motionEvent)
        }

        when (motionEvent.action) {
            MotionEvent.ACTION_UP -> {
                if ((lastY - motionEvent.rawY) > 10) {
                    return super.onInterceptTouchEvent(parent, child, motionEvent)
                }
                child.show()
                lastY = 0f
            }
            MotionEvent.ACTION_MOVE -> {
                if ((motionEvent.rawY - lastY) > 20) {
                    child.show()
                }
            }
        }
        return super.onInterceptTouchEvent(parent, child, motionEvent)
    }

    override fun onStartNestedScroll(
            coordinatorLayout: CoordinatorLayout,
            child: V,
            directTargetChild: View,
            target: View,
            axes: Int,
            type: Int
    ): Boolean {
        return axes == ViewCompat.SCROLL_AXIS_VERTICAL
    }

    override fun onNestedPreScroll(
            coordinatorLayout: CoordinatorLayout,
            child: V,
            target: View,
            dx: Int,
            dy: Int,
            consumed: IntArray,
            type: Int
    ) {
        if (child !is FloatingActionButton || child.isGone || dy <= 0) {
            return
        }

        child.hide()
    }

}
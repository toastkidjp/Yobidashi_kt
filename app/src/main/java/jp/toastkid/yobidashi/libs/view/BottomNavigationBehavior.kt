/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.libs.view

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import androidx.annotation.Keep
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.ViewCompat
import com.google.android.material.snackbar.Snackbar
import kotlin.math.max
import kotlin.math.min

/**
 * @author toastkidjp
 */
@Keep
class BottomNavigationBehavior<V : View>(context: Context, attrs: AttributeSet) :
        CoordinatorLayout.Behavior<V>(context, attrs) {

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
        super.onNestedPreScroll(coordinatorLayout, child, target, dx, dy, consumed, type)
        child.translationY = max(0f, min(child.height.toFloat(), child.translationY + dy))
    }

    override fun layoutDependsOn(parent: CoordinatorLayout, child: V, dependency: View): Boolean {
        if (dependency is Snackbar.SnackbarLayout) {
            updateSnackbar(child, dependency)
        }
        return super.layoutDependsOn(parent, child, dependency)
    }

    private fun updateSnackbar(child: View, snackbarLayout: Snackbar.SnackbarLayout) {
        val params =
                snackbarLayout.layoutParams as? CoordinatorLayout.LayoutParams ?: return

        params.anchorId = child.id
        params.anchorGravity = Gravity.TOP
        params.gravity = Gravity.TOP
        snackbarLayout.layoutParams = params
    }
}
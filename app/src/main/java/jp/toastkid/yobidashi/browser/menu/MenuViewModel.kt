/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.browser.menu

import android.graphics.Color
import android.graphics.drawable.Drawable
import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import io.reactivex.functions.Consumer
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.libs.preference.PreferenceApplier
import jp.toastkid.yobidashi.libs.view.CircleRecyclerView

/**
 * @author toastkidjp
 */
class MenuViewModel(
        private val recyclerView: CircleRecyclerView?,
        private val menuSwitch: FloatingActionButton?,
        private val onMenuClick: (Int) -> Unit,
        private val tabCountSupplier: () -> Int
) {

    private var menuDrawable: Drawable? = null

    private var previousIconColor: Int = Color.TRANSPARENT

    init {
        initialize()
    }

    /**
     * Implement for using guard.
     */
    fun initialize() {
        val activityContext = recyclerView?.context ?: return
        recyclerView.adapter = MenuAdapter(
                activityContext,
                Consumer { menu -> onMenuClick(menu.ordinal) },
                tabCountSupplier
        )
        val layoutManager = LinearLayoutManager(activityContext, RecyclerView.VERTICAL, false)
        recyclerView.layoutManager =
                layoutManager
        layoutManager.scrollToPosition(MenuAdapter.mediumPosition())
        recyclerView.setNeedLoop(true)
        menuDrawable = ContextCompat.getDrawable(activityContext, R.drawable.ic_menu)
        menuSwitch?.setOnClickListener { switchMenuVisibility() }
    }

    fun switchMenuVisibility() {
        if (recyclerView?.isVisible == true) close() else open()
    }

    private fun open() {
        menuSwitch?.hide()
        recyclerView?.visibility = View.VISIBLE
        recyclerView?.scheduleLayoutAnimation()
    }

    fun close() {
        recyclerView?.animate()?.let {
            it.cancel()
            it.alpha(0f)
                    .setDuration(350L)
                    .withEndAction {
                        recyclerView.visibility = View.GONE
                        recyclerView.alpha = 1f
                        menuSwitch?.show()
                    }
                    .start()
        }
    }

    fun onResume(additional: (MenuPos) -> Unit) {
        val activityContext = recyclerView?.context ?: return
        val preferenceApplier = PreferenceApplier(activityContext)
        recyclerView.also {
            val menuPos = preferenceApplier.menuPos()
            setGravity(menuPos, recyclerView)
            setGravity(menuPos, menuSwitch)
            additional(menuPos)
            it.requestLayout()
        }

        val newColor = preferenceApplier.colorPair().bgColor()
        if (previousIconColor != newColor) {
            menuDrawable?.also { drawable ->
                DrawableCompat.setTint(drawable, preferenceApplier.colorPair().bgColor())
                menuSwitch?.setImageDrawable(drawable)
            }
            previousIconColor = newColor
        }
    }
    
    fun isVisible() = recyclerView?.isVisible

    private fun setGravity(menuPos: MenuPos, view: View?) {
        val layoutParams = view?.layoutParams as CoordinatorLayout.LayoutParams
        layoutParams.gravity = menuPos.gravity()
    }

}
/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.menu

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.graphics.drawable.RippleDrawable
import android.os.Build
import android.view.Gravity
import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.libs.preference.PreferenceApplier
import jp.toastkid.yobidashi.libs.view.CircleRecyclerView

/**
 * @author toastkidjp
 */
class MenuBinder(
        fragmentActivity: FragmentActivity,
        private val menuViewModel: MenuViewModel?,
        private val recyclerView: CircleRecyclerView?,
        private val menuSwitch: FloatingActionButton?
) {

    private var menuAdapter: MenuAdapter? = null

    private var menuDrawable: Drawable? = null

    private var previousIconColor: Int = Color.TRANSPARENT

    init {
        fragmentActivity.let {
            menuAdapter = MenuAdapter(it, menuViewModel)
        }

        menuViewModel?.visibility?.observe(fragmentActivity, Observer { newVisible ->
            if (newVisible) open() else close()
        })

        menuViewModel?.tabCount?.observe(fragmentActivity, Observer {
            menuAdapter?.setTabCount(it)
            menuAdapter?.notifyDataSetChanged()
        })

        menuViewModel?.onResume?.observe(fragmentActivity, Observer {
            val activityContext = recyclerView?.context ?: return@Observer
            val preferenceApplier = PreferenceApplier(activityContext)
            recyclerView.also {
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
        })

        initializeWithContext(fragmentActivity)
    }

    private fun initializeWithContext(context: Context) {
        LinearSnapHelper().attachToRecyclerView(recyclerView)
        recyclerView?.adapter = menuAdapter
        val layoutManager =
                LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        recyclerView?.layoutManager = layoutManager
        layoutManager.scrollToPosition(MenuAdapter.mediumPosition())
        recyclerView?.setNeedLoop(true)
        menuDrawable = ContextCompat.getDrawable(context, R.drawable.ic_menu)
        menuSwitch?.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                (it.background as? RippleDrawable)?.also { ripple ->
                    ripple.state = arrayOf(android.R.attr.state_pressed, android.R.attr.state_enabled)
                            .toIntArray()
                    menuSwitch.postDelayed({ ripple.state = IntArray(0) }, 200)
                }
            }
            menuViewModel?.visibility?.postValue(recyclerView?.isVisible == false)
        }
    }

    private fun open() {
        val menuX: Float = menuSwitch?.x ?: 1000f
        val useLeft = menuX < 200f
        recyclerView?.layoutParams =
                (recyclerView?.layoutParams as? CoordinatorLayout.LayoutParams)?.also {
                    it.gravity = if (useLeft) Gravity.LEFT else Gravity.RIGHT
                }
        recyclerView?.setMode(useLeft)
        recyclerView?.visibility = View.VISIBLE
        recyclerView?.scheduleLayoutAnimation()
    }

    private fun close() {
        recyclerView?.animate()?.let {
            it.cancel()
            it.alpha(0f)
                    .setDuration(350L)
                    .withEndAction {
                        recyclerView.visibility = View.GONE
                        recyclerView.alpha = 1f
                    }
                    .start()
        }
    }
}
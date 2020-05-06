/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.menu

import android.annotation.SuppressLint
import android.graphics.Color
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import androidx.core.view.isVisible
import androidx.databinding.ViewStubProxy
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import jp.toastkid.yobidashi.databinding.ModuleMainMenuBinding
import jp.toastkid.yobidashi.libs.preference.PreferenceApplier
import jp.toastkid.yobidashi.libs.view.CircleRecyclerView
import jp.toastkid.yobidashi.libs.view.DraggableTouchListener
import kotlin.math.min

/**
 * @author toastkidjp
 */
class MenuBinder(
        fragmentActivity: FragmentActivity,
        private val menuViewModel: MenuViewModel?,
        private val menuStub: ViewStubProxy,
        private val menuSwitch: FloatingActionButton?
) {
    private val preferenceApplier = PreferenceApplier(fragmentActivity)

    private var menuAdapter: MenuAdapter? = null

    private var previousIconColor: Int = Color.TRANSPARENT

    private var recyclerView: CircleRecyclerView? = null

    init {
        setFabListener()

        menuViewModel?.visibility?.observe(fragmentActivity, Observer { newVisible -> // TODO Rename variable.
            if (newVisible) open() else close()
        })

        menuViewModel?.onResume?.observe(fragmentActivity, Observer {
            recyclerView?.requestLayout()

            val colorPair = preferenceApplier.colorPair()
            val newColor = colorPair.bgColor()
            if (previousIconColor != newColor) {
                previousIconColor = newColor
                colorPair.applyReverseTo(menuSwitch)
            }
        })

        menuViewModel?.resetPosition?.observe(fragmentActivity, Observer {
            menuSwitch?.let {
                it.translationX = 0f
                it.translationY = 0f
                preferenceApplier.clearMenuFabPosition()
            }
        })

        setFabPosition()
    }

    /**
     * Set FAB's listener.
     */
    @SuppressLint("ClickableViewAccessibility")
    private fun setFabListener() {
        val listener = DraggableTouchListener()
        listener.setCallback(object : DraggableTouchListener.OnNewPosition {
            override fun onNewPosition(x: Float, y: Float) {
                preferenceApplier.setNewMenuFabPosition(x, y)
            }
        })
        listener.setOnClick(object : DraggableTouchListener.OnClick {
            override fun onClick() {
                if (!menuStub.isInflated) {
                    menuStub.viewStub?.inflate()
                    recyclerView = (menuStub.binding as? ModuleMainMenuBinding)?.menusView
                    initializeWithContext()

                    menuViewModel?.switchVisibility(true)
                    return
                }
                menuViewModel?.switchVisibility(menuStub.root?.isVisible == false)
            }
        })

        menuSwitch?.setOnTouchListener(listener)

        menuSwitch?.viewTreeObserver?.addOnGlobalLayoutListener {
            val menuFabPosition = preferenceApplier.menuFabPosition()
            val displayMetrics =
                    menuSwitch.context?.resources?.displayMetrics ?: return@addOnGlobalLayoutListener
            if (menuSwitch.x > displayMetrics.widthPixels) {
                menuSwitch.x =
                        min(menuFabPosition?.first ?: 0f, displayMetrics.widthPixels.toFloat())
            }
            if (menuSwitch.y > displayMetrics.heightPixels) {
                menuSwitch.y =
                        min(menuFabPosition?.second ?: 0f, displayMetrics.heightPixels.toFloat())
            }
        }
    }

    private fun initializeWithContext() {
        LinearSnapHelper().attachToRecyclerView(recyclerView)
        (recyclerView?.context as? FragmentActivity)?.let {
            val layoutManager =
                    LinearLayoutManager(it, RecyclerView.VERTICAL, false)
            recyclerView?.layoutManager = layoutManager
            layoutManager.scrollToPosition(MenuAdapter.mediumPosition())

            menuAdapter = MenuAdapter(it, menuViewModel)
            recyclerView?.adapter = menuAdapter
        }

        recyclerView?.setNeedLoop(true)
    }

    private fun setFabPosition() {
        menuSwitch?.let {
            val fabPosition = preferenceApplier.menuFabPosition() ?: return@let
            val displayMetrics = it.context.resources.displayMetrics
            val x = when {
                fabPosition.first > displayMetrics.widthPixels.toFloat() ->
                    displayMetrics.widthPixels.toFloat()
                fabPosition.first < 0 -> 0f
                else -> fabPosition.first
            }
            val y = when {
                fabPosition.second > displayMetrics.heightPixels.toFloat() ->
                    displayMetrics.heightPixels.toFloat()
                fabPosition.second < 0 -> 0f
                else -> fabPosition.second
            }
            it.animate().x(x).y(y).setDuration(10).start()
        }
    }

    private fun open() {
        val menuX: Float = menuSwitch?.x ?: 1000f
        val useLeft = menuX < 200f
        menuStub.root?.layoutParams =
                (menuStub.root?.layoutParams as? FrameLayout.LayoutParams)?.also {
                    it.gravity = if (useLeft) Gravity.LEFT else Gravity.RIGHT
                }
        menuStub.root?.visibility = View.VISIBLE

        recyclerView?.setMode(useLeft)
        recyclerView?.scheduleLayoutAnimation()
    }

    private fun close() {
        recyclerView?.animate()?.let {
            it.cancel()
            it.alpha(0f)
                    .setDuration(350L)
                    .withEndAction {
                        menuStub.root?.visibility = View.GONE
                        menuStub.root?.alpha = 1f
                    }
                    .start()
        }
    }
}
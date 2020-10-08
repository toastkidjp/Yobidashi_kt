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
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.databinding.ViewStubProxy
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import jp.toastkid.lib.preference.PreferenceApplier
import jp.toastkid.lib.view.DraggableTouchListener
import jp.toastkid.lib.view.TextViewColorApplier
import jp.toastkid.yobidashi.databinding.ModuleMainMenuBinding
import kotlin.math.min
import kotlin.reflect.KFunction0

/**
 * @author toastkidjp
 */
class MenuBinder(
        fragmentActivity: FragmentActivity,
        private val menuViewModel: MenuViewModel?,
        private val menuStub: ViewStubProxy,
        private val menuSwitch: FloatingActionButton?,
        private val openSetting: KFunction0<Unit>
) {
    private val preferenceApplier = PreferenceApplier(fragmentActivity)

    private val textColorApplier = TextViewColorApplier()

    private var menuAdapter: MenuAdapter? = null

    private var previousIconColor: Int = Color.TRANSPARENT

    private var recyclerView: RecyclerView? = null

    init {
        setFabListener()

        menuViewModel?.visibility?.observe(fragmentActivity, Observer {
            if (it) open() else close()
        })

        menuViewModel?.onResume?.observe(fragmentActivity, Observer {
            recyclerView?.requestLayout()

            val colorPair = preferenceApplier.colorPair()
            val newColor = colorPair.bgColor()
            if (previousIconColor != newColor) {
                previousIconColor = newColor
                colorPair.applyReverseTo(menuSwitch)
            }

            applyTextColor(colorPair.fontColor(), (menuStub.binding as? ModuleMainMenuBinding)?.setting)
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
                    (menuStub.binding as? ModuleMainMenuBinding)?.also {
                        recyclerView = it.menusView
                        it.setting.setOnClickListener {
                            openSetting()
                        }
                    }

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
                    LinearLayoutManager(it, RecyclerView.HORIZONTAL, false)
            recyclerView?.layoutManager = layoutManager
            layoutManager.scrollToPosition(MenuAdapter.mediumPosition())

            menuAdapter = MenuAdapter(LayoutInflater.from(it), preferenceApplier, menuViewModel)
            recyclerView?.adapter = menuAdapter
        }

        applyTextColor(preferenceApplier.fontColor, (menuStub.binding as? ModuleMainMenuBinding)?.setting)
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
        menuStub.root?.post {
            menuSwitch?.y?.let {
                menuStub.root?.y = when {
                    it > menuSwitch.rootView.height -> menuSwitch.rootView.height.toFloat()
                    it < 0 -> 0f
                    else -> it
                }
            }

            recyclerView?.scheduleLayoutAnimation()
            menuStub.root?.animate()?.let {
                it.cancel()
                it.alpha(1f)
                        .setDuration(350L)
                        .withStartAction {
                            menuStub.root?.alpha = 0f
                            menuStub.root?.visibility = View.VISIBLE
                        }
                        .start()
            }
        }
    }

    private fun close() {
        menuStub.root?.animate()?.let {
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

    private fun applyTextColor(fontColor: Int, textView: TextView?) {
        textColorApplier(fontColor, textView)
    }

}
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
import android.widget.FrameLayout
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
class MenuPresenter(
        private val recyclerView: CircleRecyclerView?,
        private val menuSwitch: FloatingActionButton?,
        private val view: MenuContract.View
) : MenuContract.Presenter {

    private var menuDrawable: Drawable? = null

    private var previousIconColor: Int = Color.TRANSPARENT

    init {
        initialize()
        view.menuPresenter = this
    }

    /**
     * Implement for using guard.
     */
    fun initialize() {
        val activityContext = recyclerView?.context ?: return
        recyclerView.adapter = MenuAdapter(
                activityContext,
                Consumer { view.onMenuClick(it) },
                { view.onMenuLongClick(it) },
                { view.getTabCount() }
        )
        val layoutManager = LinearLayoutManager(activityContext, RecyclerView.VERTICAL, false)
        recyclerView.layoutManager =
                layoutManager
        layoutManager.scrollToPosition(MenuAdapter.mediumPosition())
        recyclerView.setNeedLoop(true)
        menuDrawable = ContextCompat.getDrawable(activityContext, R.drawable.ic_menu)
        menuSwitch?.setOnClickListener { switchMenuVisibility() }
    }

    override fun switchMenuVisibility() {
        if (recyclerView?.isVisible == true) close() else open()
    }

    private fun open() {
        recyclerView?.visibility = View.VISIBLE
        recyclerView?.scheduleLayoutAnimation()
    }

    override fun close() {
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

    override fun onResume(additional: (MenuPos) -> Unit) {
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
    
    override fun isVisible() = recyclerView?.isVisible ?: false

    override fun notifyDataSetChanged() {
        recyclerView?.adapter?.notifyDataSetChanged()
    }

    /**
     * TODO Delete and replace.
     */
    private fun setGravity(menuPos: MenuPos, view: View?) {
        val layoutParams = view?.layoutParams as FrameLayout.LayoutParams
        layoutParams.gravity = menuPos.gravity()
    }

}
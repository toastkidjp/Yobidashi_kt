/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.menu

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.libs.preference.PreferenceApplier

/**
 * @author toastkidjp
 */
internal class MenuAdapter(
        context: Context,
        private val menuViewModel: MenuViewModel?
) : RecyclerView.Adapter<MenuViewHolder>() {

    /**
     * Layout inflater.
     */
    private val inflater: LayoutInflater = LayoutInflater.from(context)

    /**
     * Menu.
     */
    private val menus: Array<Menu> = Menu.values()

    /**
     * Preference wrapper.
     */
    private val preferenceApplier = PreferenceApplier(context)

    private var tabCount = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MenuViewHolder =
            MenuViewHolder(
                    DataBindingUtil.inflate(inflater, LAYOUT_ID, parent, false)
            )

    override fun onBindViewHolder(holder: MenuViewHolder, position: Int) {
        val menu = menus[position % menus.size]

        if (menu == Menu.TAB_LIST) {
            holder.setCount(tabCount)
        } else {
            holder.hideCount()
        }

        holder.setColorPair(preferenceApplier.colorPair())
        holder.setText(menu.titleId)
        holder.setImage(menu.iconId)
        holder.setOnClick(View.OnClickListener { menuViewModel?.click?.postValue(menu) })
        holder.setOnLongClick(
                View.OnLongClickListener {
                    menuViewModel?.longClick?.postValue(menu)
                    true
                }
        )
    }

    override fun getItemCount(): Int = MAXIMUM

    fun setTabCount(count: Int) {
        tabCount = count
    }

    companion object {

        /**
         * Layout ID.
         */
        @LayoutRes
        private const val LAYOUT_ID = R.layout.item_home_menu

        /**
         * Maximum length of menus.
         */
        private val MAXIMUM = Menu.values().size * 20

        /**
         * Medium position of menus.
         */
        private val MEDIUM = MAXIMUM / 2

        /**
         * Return medium position of menus.
         * @return MEDIUM
         */
        fun mediumPosition(): Int = MEDIUM
    }
}

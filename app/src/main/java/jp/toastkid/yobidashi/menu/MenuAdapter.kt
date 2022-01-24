/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.menu

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.ListAdapter
import jp.toastkid.lib.preference.PreferenceApplier
import jp.toastkid.lib.view.list.CommonItemCallback
import jp.toastkid.yobidashi.R

/**
 * @author toastkidjp
 */
internal class MenuAdapter(
        private val inflater: LayoutInflater,
        private val preferenceApplier: PreferenceApplier,
        private val menuViewModel: MenuViewModel?
) : ListAdapter<Menu, MenuViewHolder>(
    CommonItemCallback.with({ a, b -> a.ordinal == b.ordinal }, { a, b -> a == b })
) {

    init {
        submitList(Menu.values().toList())
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MenuViewHolder =
            MenuViewHolder(
                    DataBindingUtil.inflate(inflater, LAYOUT_ID, parent, false)
            )

    override fun onBindViewHolder(holder: MenuViewHolder, position: Int) {
        val menu = getItem(position % currentList.size)
        holder.setColorPair(preferenceApplier.colorPair())
        holder.setText(menu.titleId)
        holder.setImage(menu.iconId)
        holder.setOnClick({ menuViewModel?.click(menu) })
        holder.setOnLongClick {
            menuViewModel?.longClick(menu)
            true
        }
    }

    override fun getItemCount(): Int = MAXIMUM

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

/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.browser.menu

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import io.reactivex.disposables.Disposable
import io.reactivex.functions.Consumer
import io.reactivex.subjects.PublishSubject
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.libs.preference.PreferenceApplier

/**
 * @author toastkidjp
 */
internal class MenuAdapter(
        context: Context,
        consumer: Consumer<Menu>,
        private val onLongClick: (Menu) -> Boolean,
        private val tabCountSupplier: () -> Int
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
     * Menu action subject.
     */
    private val menuSubject: PublishSubject<Menu>

    private val preferenceApplier = PreferenceApplier(context)

    /**
     * Subscription disposable.
     */
    private val disposable: Disposable?

    init {
        menuSubject = PublishSubject.create<Menu>()
        disposable = menuSubject.subscribe(consumer)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MenuViewHolder =
            MenuViewHolder(
                    DataBindingUtil.inflate(inflater, LAYOUT_ID, parent, false)
            )

    override fun onBindViewHolder(holder: MenuViewHolder, position: Int) {
        val menu = menus[position % menus.size]

        if (menu == Menu.TAB_LIST) {
            holder.setCount(tabCountSupplier())
        }

        holder.setColorPair(preferenceApplier.colorPair(), menu != Menu.SITE_SEARCH)
        holder.setText(menu.titleId)
        holder.setImage(menu.iconId)
        holder.setOnClick(View.OnClickListener { menuSubject.onNext(menu) })
        holder.setOnLongClick(View.OnLongClickListener { onLongClick(menu) })
    }

    override fun getItemCount(): Int = MAXIMUM

    /**
     * Dispose subscription.
     */
    fun dispose() {
        disposable?.dispose()
    }

    companion object {

        /**
         * Layout ID.
         */
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

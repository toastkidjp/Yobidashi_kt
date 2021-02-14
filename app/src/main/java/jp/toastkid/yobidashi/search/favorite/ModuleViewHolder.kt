/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.search.favorite

import android.view.View
import android.view.ViewGroup
import androidx.annotation.DrawableRes
import androidx.core.view.isVisible
import androidx.core.view.updateMargins
import androidx.recyclerview.widget.RecyclerView
import jp.toastkid.lib.view.SwipeViewHolder
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.databinding.ItemSearchHistoryBinding

/**
 * @author toastkidjp
 */
internal class ModuleViewHolder(private val binding: ItemSearchHistoryBinding)
    : RecyclerView.ViewHolder(binding.root), SwipeViewHolder {

    private val buttonMargin = binding.root.resources
            .getDimensionPixelSize(R.dimen.button_margin)

    init {
        binding.searchHistoryBookmark.visibility = View.GONE
    }

    fun setText(text: String?) {
        binding.searchHistoryText.text = text
    }

    fun setImageRes(@DrawableRes iconId: Int) {
        binding.searchHistoryImage.setImageResource(iconId)
    }

    fun setOnClickAdd(history: FavoriteSearch, onClickAdd: (FavoriteSearch) -> Unit) {
        binding.searchHistoryAdd.setOnClickListener { _ ->
            onClickAdd(history)
        }
    }

    fun setOnClickDelete(onClick: () -> Unit) {
        binding.delete.setOnClickListener {
            onClick()
        }
    }

    fun setAddIcon(@DrawableRes addIcon: Int) {
        binding.searchHistoryAdd.setImageResource(addIcon)
    }

    override fun getFrontView(): View = binding.front

    override fun isButtonVisible(): Boolean = binding.delete.isVisible

    override fun showButton() {
        binding.delete.visibility = View.VISIBLE
        updateRightMargin(buttonMargin)
    }

    override fun hideButton() {
        binding.delete.visibility = View.INVISIBLE
        updateRightMargin(0)
    }

    private fun updateRightMargin(margin: Int) {
        val marginLayoutParams = binding.front.layoutParams as? ViewGroup.MarginLayoutParams
        marginLayoutParams?.rightMargin = margin
        binding.front.layoutParams = marginLayoutParams
        marginLayoutParams?.updateMargins()
    }
}

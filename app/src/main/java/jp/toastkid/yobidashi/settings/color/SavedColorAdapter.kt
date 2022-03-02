/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.settings.color

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import jp.toastkid.lib.ContentViewModel
import jp.toastkid.lib.view.list.CommonItemCallback
import jp.toastkid.yobidashi.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * @author toastkidjp
 */
internal class SavedColorAdapter(
        private val layoutInflater: LayoutInflater,
        private val repository: SavedColorRepository,
        private val contentViewModel: ContentViewModel,
        private val commitNewColor: (Int, Int) -> Unit
) : ListAdapter<SavedColor, SavedColorHolder>(
    CommonItemCallback.with<SavedColor>({ a, b -> a.id == b.id }, { a, b -> a == b })
) {

    override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
    ): SavedColorHolder {
        return SavedColorHolder(layoutInflater.inflate(R.layout.item_saved_color, parent, false))
    }

    override fun onBindViewHolder(holder: SavedColorHolder, position: Int) {
        bindView(holder, currentList[position])
    }

    /**
     * Bind value and action to holder's view.
     *
     * @param holder Holder
     * @param color  [SavedColor] object
     */
    private fun bindView(holder: SavedColorHolder, color: SavedColor) {
        color.setTo(holder.textView)
        holder.textView.setOnClickListener { commitNewColor(color.bgColor, color.fontColor) }
        holder.remove.setOnClickListener {
            CoroutineScope(Dispatchers.IO).launch {
                repository.delete(color)
                deleteAt(color)
            }
            contentViewModel.snackShort(R.string.settings_color_delete)
        }
    }

    fun refresh() {
        CoroutineScope(Dispatchers.Main).launch {
            val list = withContext(Dispatchers.IO) { (repository.findAll()) }
            submitList(list)
        }
    }

    private fun deleteAt(savedColor: SavedColor) {
        CoroutineScope(Dispatchers.Main).launch {
            withContext(Dispatchers.IO) {
                repository.delete(savedColor)
            }
            refresh()
        }
    }

    fun clear() {
        CoroutineScope(Dispatchers.Main).launch {
            withContext(Dispatchers.IO) {
                repository.deleteAll()
            }
            submitList(emptyList())

            contentViewModel.snackShort(R.string.settings_color_delete)
        }
    }

    fun reload() {
        submitList(repository.findAll())
    }
}
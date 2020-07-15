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
import androidx.recyclerview.widget.RecyclerView
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
        private val commitNewColor: (Int, Int) -> Unit
) : RecyclerView.Adapter<SavedColorHolder>() {

    private val items = mutableListOf<SavedColor>()

    override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
    ): SavedColorHolder {
        return SavedColorHolder(layoutInflater.inflate(R.layout.item_saved_color, parent, false))
    }

    override fun onBindViewHolder(holder: SavedColorHolder, position: Int) {
        bindView(holder, items[position])
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
            // TODO snackShort(R.string.settings_color_delete)
        }
    }

    override fun getItemCount(): Int = items.count()

    fun refresh() {
        items.clear()
        CoroutineScope(Dispatchers.Main).launch {
            withContext(Dispatchers.IO) { repository.findAll().forEach { items.add(it) } }
            notifyDataSetChanged()
        }
    }

    fun deleteAt(savedColor: SavedColor) {
        CoroutineScope(Dispatchers.Main).launch {
            withContext(Dispatchers.IO) {
                repository.delete(savedColor)
                items.remove(savedColor)
            }
            notifyDataSetChanged()
        }
    }

    fun add(savedColor: SavedColor) {
        items.add(savedColor)
    }

    fun clear() {
        CoroutineScope(Dispatchers.Main).launch {
            withContext(Dispatchers.IO) {
                repository.deleteAll()
                items.clear()
            }

            notifyDataSetChanged()

            /*TODO
            val root = binding?.root ?: return@launch
            Toaster.snackShort(
                    root,
                    R.string.settings_color_delete,
                    colorPair()
            )
             */
        }
    }
}
/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.search.favorite

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.ListAdapter
import jp.toastkid.lib.color.IconColorFinder
import jp.toastkid.lib.view.list.CommonItemCallback
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.search.SearchAction
import jp.toastkid.yobidashi.search.SearchFragmentViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.util.ArrayList
import kotlin.math.min

/**
 * ModuleAdapter of search history list.
 *
 * @param context
 * @param favoriteSearchRepository Relation
 * @param onVisibilityChanged On changed visibility callback
 * @param maxItemCount default is -1 (Unlimited)
 *
 * @author toastkidjp
 */
internal class ModuleAdapter(
        context: Context,
        private val favoriteSearchRepository: FavoriteSearchRepository,
        private val onVisibilityChanged: (Boolean) -> Unit,
        private val maxItemCount: Int = -1
) : ListAdapter<FavoriteSearch, ModuleViewHolder>(
    CommonItemCallback.with({ a, b -> a.id == b.id }, { a, b -> a == b })
) {

    /**
     * Layout inflater.
     */
    private val inflater: LayoutInflater = LayoutInflater.from(context)

    /**
     * Selected items.
     */
    private val selected: MutableList<FavoriteSearch> = ArrayList(5)

    private var viewModel: SearchFragmentViewModel? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ModuleViewHolder {
        return ModuleViewHolder(DataBindingUtil.inflate(
                inflater, LAYOUT_ID, parent, false))
    }

    override fun onBindViewHolder(holder: ModuleViewHolder, position: Int) {
        val favorite = getItem(position)
        holder.setText(favorite.query)
        holder.itemView.setOnClickListener {
            try {
                val query = favorite.query ?: return@setOnClickListener
                val category = favorite.category ?: return@setOnClickListener
                viewModel?.searchWithCategory(query, category)
            } catch (e: Exception) {
                Timber.e(e)
            }
        }
        holder.setOnClickAdd(favorite) { viewModel?.putQuery(it.query ?: "") }

        holder.setOnClickDelete { remove(favorite) }

        holder.setAddIcon(R.drawable.ic_add_circle_search)
        holder.setIconColor(IconColorFinder.from(holder.itemView).invoke())

        holder.setImageRes(jp.toastkid.search.SearchCategory.findByCategory(favorite.category).iconId)
        holder.itemView.setOnLongClickListener { v ->
            SearchAction(
                    v.context,
                    favorite.category ?: "",
                    favorite.query ?: "",
                    onBackground = true,
                    saveHistory = true
            ).invoke()
            true
        }
        holder.hideButton()
    }

    /**
     * Execute query.
     *
     * @param s query word [String]
     * @return [Job]
     */
    fun query(s: CharSequence): Job {
        return CoroutineScope(Dispatchers.Main).launch {
            val items = withContext(Dispatchers.IO) {
                if (s.isNotBlank()) {
                    favoriteSearchRepository.select("$s%")
                } else {
                    favoriteSearchRepository.find(maxItemCount)
                }
            }

            submitList(items)
            onVisibilityChanged(items.isNotEmpty())
        }
    }

    /**
     * Remove item with position.
     *
     * @param position
     */
    fun removeAt(position: Int): Job {
        val item = getItem(position)
        return remove(item, position)
    }

    private fun remove(item: FavoriteSearch, position: Int = -1): Job {
        return CoroutineScope(Dispatchers.Main).launch {
            val copy = mutableListOf<FavoriteSearch>().also { it.addAll(currentList) }
            withContext(Dispatchers.IO) {
                favoriteSearchRepository.delete(item)
                copy.remove(item)
            }

            submitList(copy)
            if (isEmpty()) {
                onVisibilityChanged(false)
            }
        }
    }

    /**
     * Return selected item is empty.
     *
     * @return if this adapter is empty, return true
     */
    private fun isEmpty(): Boolean = itemCount == 0

    /**
     * Clear selected items.
     */
    fun clear() {
        submitList(emptyList())
    }

    /**
     * Add passed history item to selected list.
     *
     * @param history
     */
    private fun add(history: FavoriteSearch) {
        selected.add(history)
    }

    fun refresh(): Job {
        return CoroutineScope(Dispatchers.Main).launch {
            val items = withContext(Dispatchers.IO) { favoriteSearchRepository.findAll() }
            submitList(items)
        }
    }

    fun setViewModel(viewModel: SearchFragmentViewModel) {
        this.viewModel = viewModel
    }

    override fun getItemCount(): Int {
        return if (maxItemCount == -1) currentList.size else min(maxItemCount, currentList.size)
    }

    companion object {

        @LayoutRes
        private const val LAYOUT_ID = R.layout.item_search_history

    }
}

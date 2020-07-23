/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.article_viewer.bookmark

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import jp.toastkid.article_viewer.R
import jp.toastkid.article_viewer.article.ArticleRepository
import jp.toastkid.article_viewer.article.data.AppDatabase
import jp.toastkid.article_viewer.article.list.Adapter
import jp.toastkid.article_viewer.databinding.FragmentArticleListBinding
import jp.toastkid.lib.ContentScrollable
import jp.toastkid.lib.ContentViewModel
import jp.toastkid.lib.preference.PreferenceApplier
import jp.toastkid.lib.view.RecyclerViewScroller
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * @author toastkidjp
 */
class BookmarkFragment : Fragment(), ContentScrollable {

    /**
     * [RecyclerView]'s adapter.
     */
    private lateinit var adapter: Adapter

    /**
     * Preferences wrapper.
     */
    private lateinit var preferencesWrapper: PreferenceApplier

    /**
     * Use for reading article data from DB.
     */
    private lateinit var articleRepository: ArticleRepository

    private lateinit var binding: FragmentArticleListBinding

    /**
     * Use for clean up subscriptions.
     */
    private val disposables = Job()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        context?.let { initializeRepository(it) }

        setHasOptionsMenu(true)
    }

    /**
     * Initialize repository.
     *
     * @param activityContext [Context]
     */
    private fun initializeRepository(activityContext: Context) {
        val dataBase = AppDatabase.find(activityContext)

        articleRepository = dataBase.articleRepository()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)

        context?.let { preferencesWrapper = PreferenceApplier(it) }

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_article_list, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val activityContext = context ?: return

        adapter = Adapter(
            LayoutInflater.from(activityContext),
            { title ->
                CoroutineScope(Dispatchers.Main).launch(disposables) {
                    ViewModelProvider(requireActivity()).get(ContentViewModel::class.java)
                            .newArticle(title)
                }
            },
            { }
        )
        binding.results.adapter = adapter
        binding.results.layoutManager = LinearLayoutManager(activityContext, RecyclerView.VERTICAL, false)
        CoroutineScope(Dispatchers.Main).launch {
            withContext(Dispatchers.IO) {
                val database = AppDatabase.find(activityContext)
                val articleIds = database.bookmarkRepository().allArticleIds()
                articleRepository.findByIds(articleIds).forEach { adapter.add(it) }
            }
            adapter.notifyDataSetChanged()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, menuInflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, menuInflater)
        menuInflater?.inflate(R.menu.menu_article_list, menu)
        menu?.findItem(R.id.action_switch_title_filter)?.isChecked = preferencesWrapper.useTitleFilter()
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.action_switch_title_filter -> {
            val newState = !item.isChecked
            preferencesWrapper.switchUseTitleFilter(newState)
            item.isChecked = newState
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    override fun toTop() {
        RecyclerViewScroller.toTop(binding.results, binding.results.adapter?.itemCount ?: 0)
    }

    override fun toBottom() {
        RecyclerViewScroller.toBottom(binding.results, binding.results.adapter?.itemCount ?: 0)
    }

    override fun onDestroy() {
        super.onDestroy()
        disposables.cancel()
    }
}
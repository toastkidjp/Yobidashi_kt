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
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import jp.toastkid.article_viewer.R
import jp.toastkid.article_viewer.article.ArticleRepository
import jp.toastkid.article_viewer.article.data.AppDatabase
import jp.toastkid.article_viewer.article.list.ArticleListFragmentViewModel
import jp.toastkid.article_viewer.article.list.ArticleListFragmentViewModelFactory
import jp.toastkid.article_viewer.article.list.menu.BookmarkListMenuPopupActionUseCase
import jp.toastkid.article_viewer.article.list.view.ArticleListUi
import jp.toastkid.article_viewer.bookmark.repository.BookmarkRepository
import jp.toastkid.lib.ContentScrollable
import jp.toastkid.lib.ContentViewModel
import jp.toastkid.lib.preference.PreferenceApplier
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
     * Preferences wrapper.
     */
    private lateinit var preferencesWrapper: PreferenceApplier

    /**
     * Use for reading article data from DB.
     */
    private lateinit var articleRepository: ArticleRepository

    private var scrollState: LazyListState? = null

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

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)

        val activity = activity
            ?: return super.onCreateView(inflater, container, savedInstanceState)

        preferencesWrapper = PreferenceApplier(activity)

        val composeView = ComposeView(activity)
        composeView.setViewCompositionStrategy(
            ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed
        )
        val contentViewModel = ViewModelProvider(activity).get(ContentViewModel::class.java)
        val bookmarkRepository = AppDatabase.find(activity).bookmarkRepository()

        val viewModel = ArticleListFragmentViewModelFactory(
            articleRepository,
            bookmarkRepository,
            preferencesWrapper
        )
            .create(ArticleListFragmentViewModel::class.java)

        val menuPopupUseCase = BookmarkListMenuPopupActionUseCase(
            bookmarkRepository,
            {
                contentViewModel.snackShort("Deleted.")
                CoroutineScope(Dispatchers.IO).launch {
                    viewModel.bookmark()
                }
                closeOnEmpty(bookmarkRepository, contentViewModel)
            }
        )

        viewModel.dataSource.observe(viewLifecycleOwner, {
            composeView.setContent {
                val listState = rememberLazyListState()
                this.scrollState = listState
                ArticleListUi(
                    it?.flow,
                    listState,
                    contentViewModel,
                    menuPopupUseCase,
                    preferencesWrapper.color
                )
            }

            closeOnEmpty(bookmarkRepository, contentViewModel)
        })

        CoroutineScope(Dispatchers.IO).launch {
            viewModel.bookmark()
        }

        return composeView
    }

    private fun closeOnEmpty(
        bookmarkRepository: BookmarkRepository,
        contentViewModel: ContentViewModel
    ) {
        CoroutineScope(Dispatchers.Main).launch(disposables) {
            val count = withContext(Dispatchers.IO) {
                bookmarkRepository.count()
            }
            if (count == 0) {
                contentViewModel.snackShort("Bookmark list is empty.")
                activity?.supportFragmentManager?.popBackStack()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, menuInflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, menuInflater)
        menuInflater.inflate(R.menu.menu_article_list, menu)
        menu.findItem(R.id.action_switch_title_filter)?.isChecked =
            preferencesWrapper.useTitleFilter()
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
        CoroutineScope(Dispatchers.Main).launch {
            scrollState?.scrollToItem(0, 0)
        }
    }

    override fun toBottom() {
        CoroutineScope(Dispatchers.Main).launch {
            scrollState?.scrollToItem(scrollState?.layoutInfo?.totalItemsCount ?: 0, 0)
        }
    }

    override fun onDetach() {
        disposables.cancel()
        super.onDetach()
    }
}
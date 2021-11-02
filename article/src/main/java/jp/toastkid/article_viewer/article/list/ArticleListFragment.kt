/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.article_viewer.article.list

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.LayoutRes
import androidx.core.graphics.ColorUtils
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import jp.toastkid.article_viewer.R
import jp.toastkid.article_viewer.article.ArticleRepository
import jp.toastkid.article_viewer.article.data.AppDatabase
import jp.toastkid.article_viewer.article.list.date.DateFilterDialogFragment
import jp.toastkid.article_viewer.article.list.date.FilterByMonthUseCase
import jp.toastkid.article_viewer.article.list.listener.ArticleLoadStateListener
import jp.toastkid.article_viewer.article.list.menu.ArticleListMenuPopupActionUseCase
import jp.toastkid.article_viewer.article.list.menu.MenuPopup
import jp.toastkid.article_viewer.article.list.sort.Sort
import jp.toastkid.article_viewer.article.list.sort.SortSettingDialogFragment
import jp.toastkid.article_viewer.article.list.usecase.UpdateUseCase
import jp.toastkid.article_viewer.bookmark.BookmarkFragment
import jp.toastkid.article_viewer.databinding.AppBarArticleListBinding
import jp.toastkid.article_viewer.databinding.FragmentArticleListBinding
import jp.toastkid.article_viewer.zip.ZipFileChooserIntentFactory
import jp.toastkid.article_viewer.zip.ZipLoaderService
import jp.toastkid.lib.AppBarViewModel
import jp.toastkid.lib.ContentScrollable
import jp.toastkid.lib.ContentViewModel
import jp.toastkid.lib.input.Inputs
import jp.toastkid.lib.preference.PreferenceApplier
import jp.toastkid.lib.tab.OnBackCloseableTabUiFragment
import jp.toastkid.lib.view.RecyclerViewScroller
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Article list fragment.
 *
 * @author toastkidjp
 */
class ArticleListFragment : Fragment(), ContentScrollable, OnBackCloseableTabUiFragment {

    /**
     * List item adapter.
     */
    private lateinit var adapter: Adapter

    private lateinit var binding: FragmentArticleListBinding

    private lateinit var appBarBinding: AppBarArticleListBinding

    /**
     * Preferences wrapper.
     */
    private lateinit var preferencesWrapper: PreferenceApplier

    /**
     * Use for read articles from DB.
     */
    private lateinit var articleRepository: ArticleRepository

    /**
     * Use for receiving broadcast.
     */
    private val progressBroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(p0: Context?, p1: Intent?) {
            viewModel?.hideProgress()
            showFeedback()
        }

        private fun showFeedback() {
            contentViewModel?.snackShort(R.string.message_done_import)
        }
    }

    private var contentViewModel: ContentViewModel? = null

    private var viewModel: ArticleListFragmentViewModel? = null

    private var searchUseCase: ArticleSearchUseCase? = null

    private val inputChannel = Channel<String>()

    private val setTargetLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode != Activity.RESULT_OK) {
                return@registerForActivityResult
            }

            UpdateUseCase(viewModel) { context }.invokeIfNeed(it.data?.data)
        }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        preferencesWrapper = PreferenceApplier(context)

        context.registerReceiver(
                progressBroadcastReceiver,
                ZipLoaderService.makeProgressBroadcastIntentFilter()
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        context?.let {
            initializeRepository(it)
        }

        setHasOptionsMenu(true)
    }

    private fun initializeRepository(activityContext: Context) {
        val dataBase = AppDatabase.find(activityContext)

        articleRepository = dataBase.articleRepository()
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)
        binding = DataBindingUtil.inflate(
                inflater, LAYOUT_ID, container, false)
        appBarBinding = DataBindingUtil.inflate(
                inflater, R.layout.app_bar_article_list, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val activityContext = activity ?: return

        contentViewModel = ViewModelProvider(activityContext).get(ContentViewModel::class.java)

        val menuPopup = MenuPopup(
                activityContext,
                ArticleListMenuPopupActionUseCase(
                        articleRepository,
                        AppDatabase.find(activityContext).bookmarkRepository(),
                    {
                        adapter.refresh()
                        contentViewModel?.snackWithAction(
                            "Deleted: \"${it.title}\".",
                            "UNDO"
                        ) { CoroutineScope(Dispatchers.IO).launch { articleRepository.insert(it) } }
                    }
                )
        )

        adapter = Adapter(
                LayoutInflater.from(context),
                { contentViewModel?.newArticle(it) },
                { contentViewModel?.newArticleOnBackground(it) },
                { itemView, searchResult -> menuPopup.show(itemView, searchResult) }
        )

        binding.results.adapter = adapter
        binding.results.layoutManager = LinearLayoutManager(activityContext, RecyclerView.VERTICAL, false)

        activity?.let {
            val activityViewModel = ViewModelProvider(it).get(ArticleListFragmentViewModel::class.java)
            activityViewModel.search.observe(it, { searchInput ->
                searchUseCase?.search(searchInput)
                if (appBarBinding.input.text.isNullOrEmpty()) {
                    appBarBinding.input.setText(searchInput)
                    appBarBinding.searchClear.isVisible = searchInput?.length != 0
                }
            })

            appBarBinding.input.setOnEditorActionListener { textView, _, _ ->
                val keyword = textView.text.toString()
                activityViewModel.search(keyword)
                Inputs.hideKeyboard(appBarBinding.input)
                true
            }
        }

        appBarBinding.searchClear.setOnClickListener {
            appBarBinding.input.setText("")
        }

        appBarBinding.input.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) = Unit

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) = Unit

            override fun onTextChanged(charSequence: CharSequence?, p1: Int, p2: Int, p3: Int) {
                CoroutineScope(Dispatchers.Default).launch {
                    inputChannel.send(charSequence.toString())
                }

                appBarBinding.searchClear.isVisible = charSequence?.length != 0
            }

        })

        CoroutineScope(Dispatchers.Default).launch {
            inputChannel.receiveAsFlow()
                    .distinctUntilChanged()
                    .debounce(1400L)
                    .collect {
                        withContext(Dispatchers.Main) {
                            searchUseCase?.filter(it)
                        }
                    }
        }

        viewModel = ViewModelProvider(this).get(ArticleListFragmentViewModel::class.java)
        viewModel?.progressVisibility?.observe(viewLifecycleOwner, {
            it?.getContentIfNotHandled()?.let { isVisible ->
                binding.progressCircular.isVisible = isVisible
            }
        })
        viewModel?.progress?.observe(viewLifecycleOwner, {
            it?.getContentIfNotHandled()?.let { message ->
                appBarBinding.searchResult.text = message
            }
        })
        viewModel?.messageId?.observe(viewLifecycleOwner, {
            it?.getContentIfNotHandled()?.let { messageId ->
                appBarBinding.searchResult.setText(messageId)
            }
        })
        viewModel?.sort?.observe(viewLifecycleOwner, {
            it?.getContentIfNotHandled()?.let {
                searchUseCase?.all()
            }
        })
        viewModel?.filter?.observe(viewLifecycleOwner, {
            val keyword = it ?: return@observe
            searchUseCase?.filter(keyword, true)
        })

        parentFragmentManager.setFragmentResultListener(
            "sorting",
            viewLifecycleOwner,
            { key, result ->
                val sort = result[key] as? Sort ?: return@setFragmentResultListener
                viewModel?.sort(sort)
            }
        )

        parentFragmentManager.setFragmentResultListener(
            "date_filter",
            viewLifecycleOwner,
            { _, result ->
                val year = result.getInt("year")
                val month = result.getInt("month")
                FilterByMonthUseCase(
                    ViewModelProvider(this).get(ArticleListFragmentViewModel::class.java)
                ).invoke(year, month)
            }
        )

        searchUseCase = ArticleSearchUseCase(
                ListLoaderUseCase(adapter),
                articleRepository,
                preferencesWrapper
        )

        adapter.addLoadStateListener(
            ArticleLoadStateListener(contentViewModel, { adapter.itemCount }, { activityContext.getString(it) })
        )

        searchUseCase?.search(appBarBinding.input.text?.toString())
    }

    override fun onResume() {
        super.onResume()
        preferencesWrapper.colorPair().setTo(appBarBinding.input)

        val buttonColor = ColorUtils.setAlphaComponent(preferencesWrapper.fontColor, 196)
        appBarBinding.input.setHintTextColor(buttonColor)
        appBarBinding.searchClear.setColorFilter(buttonColor)

        activity?.let {
            ViewModelProvider(it).get(AppBarViewModel::class.java)
                    .replace(appBarBinding.root)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, menuInflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, menuInflater)
        menuInflater.inflate(R.menu.menu_article_list, menu)
        menu.findItem(R.id.action_switch_title_filter)?.isChecked =
            preferencesWrapper.useTitleFilter()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_all_article -> {
                searchUseCase?.all()
                activity?.let {
                    ViewModelProvider(it).get(ArticleListFragmentViewModel::class.java)
                        .search(null)
                }
                true
            }
            R.id.action_bookmark -> {
                contentViewModel?.nextFragment(BookmarkFragment::class.java)
                true
            }
            R.id.action_set_target -> {
                setTargetLauncher.launch(ZipFileChooserIntentFactory()())
                true
            }
            R.id.action_sort -> {
                val dialogFragment = SortSettingDialogFragment()
                dialogFragment.show(parentFragmentManager, "")
                true
            }
            R.id.action_date_filter -> {
                val dateFilterDialogFragment = DateFilterDialogFragment()
                dateFilterDialogFragment.show(parentFragmentManager, "")
                true
            }
            R.id.action_switch_title_filter -> {
                val newState = !item.isChecked
                preferencesWrapper.switchUseTitleFilter(newState)
                item.isChecked = newState
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun toTop() {
        RecyclerViewScroller.toTop(binding.results, adapter.itemCount)
    }

    override fun toBottom() {
        RecyclerViewScroller.toBottom(binding.results, adapter.itemCount)
    }

    override fun onDetach() {
        searchUseCase?.dispose()
        inputChannel.cancel()
        context?.unregisterReceiver(progressBroadcastReceiver)
        setTargetLauncher.unregister()
        parentFragmentManager.clearFragmentResultListener("sorting")
        parentFragmentManager.clearFragmentResultListener("date_filter")
        super.onDetach()
    }

    companion object {

        @LayoutRes
        private val LAYOUT_ID = R.layout.fragment_article_list

        private const val REQUEST_CODE = 1

    }
}
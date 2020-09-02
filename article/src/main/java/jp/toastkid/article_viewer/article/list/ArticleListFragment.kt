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
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.core.graphics.ColorUtils
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import jp.toastkid.article_viewer.R
import jp.toastkid.article_viewer.article.ArticleRepository
import jp.toastkid.article_viewer.article.data.AppDatabase
import jp.toastkid.article_viewer.article.list.menu.ArticleListMenuPopupActionUseCase
import jp.toastkid.article_viewer.article.list.menu.MenuPopup
import jp.toastkid.article_viewer.article.list.sort.SortSettingDialogFragment
import jp.toastkid.article_viewer.bookmark.BookmarkFragment
import jp.toastkid.article_viewer.databinding.AppBarArticleListBinding
import jp.toastkid.article_viewer.databinding.FragmentArticleListBinding
import jp.toastkid.article_viewer.zip.ZipFileChooserIntentFactory
import jp.toastkid.article_viewer.zip.ZipLoaderService
import jp.toastkid.lib.AppBarViewModel
import jp.toastkid.lib.ContentScrollable
import jp.toastkid.lib.ContentViewModel
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
            contentViewModel?.snackWithAction(
                    getString(R.string.message_done_import),
                    getString(R.string.reload)
            ) { searchUseCase?.all() }
        }
    }

    private var contentViewModel: ContentViewModel? = null

    private var viewModel: ArticleListFragmentViewModel? = null

    private var searchUseCase: ArticleSearchUseCase? = null

    private val inputChannel = Channel<String>()

    override fun onAttach(context: Context) {
        super.onAttach(context)

        preferencesWrapper = PreferenceApplier(context)

        context.registerReceiver(
                progressBroadcastReceiver,
                ZipLoaderService.makeProgressBroadcastIntentFilter()
        )

        retainInstance = true
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
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        binding = DataBindingUtil.inflate(
                inflater, LAYOUT_ID, container, false)
        appBarBinding = DataBindingUtil.inflate(
                inflater, R.layout.app_bar_article_list, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val activityContext = context ?: return

        contentViewModel = ViewModelProvider(requireActivity()).get(ContentViewModel::class.java)

        val menuPopup = MenuPopup(
                activityContext,
                ArticleListMenuPopupActionUseCase(
                        articleRepository,
                        AppDatabase.find(activityContext).bookmarkRepository()
                ) {
                    adapter.refresh()
                    contentViewModel?.snackWithAction(
                            "Deleted: \"${it.title}\".",
                            "UNDO"
                    ) { CoroutineScope(Dispatchers.IO).launch { articleRepository.insert(it) } }
                }
        )

        adapter = Adapter(
                LayoutInflater.from(context),
                { contentViewModel?.newArticle(it) },
                { contentViewModel?.newArticleOnBackground(it) },
                { itemView, searchResult -> menuPopup.show(itemView, searchResult) }
        )

        binding.results.adapter = adapter
        binding.results.layoutManager = LinearLayoutManager(activityContext, RecyclerView.VERTICAL, false)

        appBarBinding.input.setOnEditorActionListener { textView, _, _ ->
            val keyword = textView.text.toString()
            if (keyword.isBlank()) {
                return@setOnEditorActionListener true
            }
            searchUseCase?.search(keyword)
            true
        }

        appBarBinding.input.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) = Unit

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) = Unit

            override fun onTextChanged(charSequence: CharSequence?, p1: Int, p2: Int, p3: Int) {
                CoroutineScope(Dispatchers.Default).launch {
                    inputChannel.send(charSequence.toString())
                }
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
        viewModel?.progressVisibility?.observe(viewLifecycleOwner, Observer {
            it?.getContentIfNotHandled()?.let { isVisible ->
                binding.progressCircular.isVisible = isVisible
            }
        })
        viewModel?.progress?.observe(viewLifecycleOwner, Observer {
            it?.getContentIfNotHandled()?.let { message ->
                appBarBinding.searchResult.text = message
            }
        })
        viewModel?.messageId?.observe(viewLifecycleOwner, Observer {
            it?.getContentIfNotHandled()?.let { messageId ->
                appBarBinding.searchResult.setText(messageId)
            }
        })
        viewModel?.sort?.observe(viewLifecycleOwner, Observer {
            it?.getContentIfNotHandled()?.let { sort ->
                searchUseCase?.all()
            }
        })

        searchUseCase = ArticleSearchUseCase(
                ListLoaderUseCase(adapter),
                articleRepository,
                preferencesWrapper
        )

        searchUseCase?.all()
    }

    override fun onResume() {
        super.onResume()
        preferencesWrapper.colorPair().setTo(appBarBinding.input)
        appBarBinding.input.setHintTextColor(ColorUtils.setAlphaComponent(preferencesWrapper.fontColor, 196))
        ViewModelProvider(requireActivity()).get(AppBarViewModel::class.java)
                .replace(appBarBinding.root)
    }

    override fun onCreateOptionsMenu(menu: Menu, menuInflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, menuInflater)
        menuInflater.inflate(R.menu.menu_article_list, menu)
        menu.findItem(R.id.action_switch_title_filter)?.isChecked = preferencesWrapper.useTitleFilter()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_all_article -> {
                searchUseCase?.all()
                true
            }
            R.id.action_bookmark -> {
                contentViewModel?.nextFragment(BookmarkFragment::class.java)
                true
            }
            R.id.action_set_target -> {
                startActivityForResult(ZipFileChooserIntentFactory()(), 1)
                true
            }
            R.id.action_sort -> {
                val dialogFragment = SortSettingDialogFragment()
                dialogFragment.setTargetFragment(this, 1)
                dialogFragment.show(parentFragmentManager, "")
                true
            }
            R.id.action_switch_title_filter -> {
                val newState = !item.isChecked
                preferencesWrapper.switchUseTitleFilter(newState)
                item.isChecked = newState
                true
            }
            /* TODO Implement reload menu
            it.registerReceiver(
                progressBroadcastReceiver,
                ZipLoaderService.makeProgressBroadcastIntentFilter()
            )
             */
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1 && resultCode == Activity.RESULT_OK) {
            //PreferencesWrapper(this).setTarget(data?.data?.toString())
            val uri = data?.data ?: return
            updateIfNeed(uri)
        }
    }

    private fun updateIfNeed(target: Uri?) {
        if (target == null) {
            return
        }

        viewModel?.showProgress()

        ZipLoaderService.start(requireContext(), target)
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
        super.onDetach()
    }

    companion object {

        @LayoutRes
        private val LAYOUT_ID = R.layout.fragment_article_list

    }
}
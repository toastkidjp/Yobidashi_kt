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
import androidx.annotation.UiThread
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.toObservable
import io.reactivex.schedulers.Schedulers
import jp.toastkid.article_viewer.R
import jp.toastkid.article_viewer.article.ArticleRepository
import jp.toastkid.article_viewer.article.data.AppDatabase
import jp.toastkid.article_viewer.article.detail.ContentViewerFragment
import jp.toastkid.article_viewer.common.FragmentControl
import jp.toastkid.article_viewer.common.ProgressCallback
import jp.toastkid.article_viewer.common.SearchFunction
import jp.toastkid.article_viewer.databinding.AppBarArticleListBinding
import jp.toastkid.article_viewer.databinding.FragmentArticleListBinding
import jp.toastkid.article_viewer.tokenizer.NgramTokenizer
import jp.toastkid.article_viewer.zip.ZipLoaderService
import jp.toastkid.lib.AppBarViewModel
import jp.toastkid.lib.ContentViewModel
import jp.toastkid.lib.FileExtractorFromUri
import jp.toastkid.lib.preference.PreferenceApplier
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
import timber.log.Timber

/**
 * Article list fragment.
 *
 * @author toastkidjp
 */
class ArticleListFragment : Fragment(), SearchFunction, ProgressCallback {

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
            progressCallback.hideProgress()
            all()
        }
    }

    /**
     * Progress callback.
     */
    private lateinit var progressCallback: ProgressCallback

    /**
     * Use for switching fragment.
     */
    private var fragmentControl: FragmentControl? = null

    private val tokenizer = NgramTokenizer()

    /**
     * [CompositeDisposable].
     */
    private val disposables = CompositeDisposable()

    override fun onAttach(context: Context) {
        super.onAttach(context)

        preferencesWrapper = PreferenceApplier(context)

        progressCallback = this

        context.registerReceiver(
                progressBroadcastReceiver,
                ZipLoaderService.makeProgressBroadcastIntentFilter()
        )

        if (context is FragmentControl) {
            fragmentControl = context
        }

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

        articleRepository = dataBase.diaryRepository()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_article_list, container, false)
        appBarBinding = DataBindingUtil.inflate(inflater, R.layout.app_bar_article_list, container, false)
        return binding.root
    }

    private var contentViewModel: ContentViewModel? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val activityContext = context ?: return

        contentViewModel = ViewModelProvider(requireActivity()).get(ContentViewModel::class.java)

        adapter = Adapter(
                LayoutInflater.from(context),
                { title ->
                    Maybe.fromCallable { articleRepository.findContentByTitle(title) }
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(
                                    { content ->
                                        if (content.isNullOrBlank()) {
                                            return@subscribe
                                        }
                                        contentViewModel
                                                ?.nextFragment(ContentViewerFragment.make(title, content))
                                    },
                                    Timber::e
                            )
                            .addTo(disposables)
                },
                {
                    /*if (preferencesWrapper.containsBookmark(it)) {
                        Snackbar.make(results, "「$it」 is already added.", Snackbar.LENGTH_SHORT).show()
                        return@Adapter
                    }
                    preferencesWrapper.addBookmark(it)
                    Snackbar.make(results, "It has added $it.", Snackbar.LENGTH_SHORT).show()*/
                }
        )

        binding.results.adapter = adapter
        binding.results.layoutManager = LinearLayoutManager(activityContext, RecyclerView.VERTICAL, false)

        appBarBinding.input.setOnEditorActionListener { textView, _, _ ->
            val keyword = textView.text.toString()
            if (keyword.isBlank()) {
                return@setOnEditorActionListener true
            }
            search(keyword)
            true
        }

        val inputChannel = Channel<String>()
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
                            filter(it)
                        }
                    }
        }

        all()
    }

    override fun onResume() {
        super.onResume()
        preferencesWrapper.colorPair().setTo(appBarBinding.input)
        ViewModelProvider(requireActivity()).get(AppBarViewModel::class.java).replace(appBarBinding.root)
    }

    fun all() {
        query(
            Maybe.fromCallable { articleRepository.getAll() }
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.computation())
                .flatMapObservable { it.toObservable() }
        )
    }

    override fun search(keyword: String?) {
        if (keyword.isNullOrBlank()) {
            return
        }

        query(
            Maybe.fromCallable { articleRepository.search("${tokenizer(keyword, 2)}") }
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.computation())
                .flatMapObservable { it.toObservable() }
        )
    }

    override fun filter(keyword: String?) {
        /*if (!preferencesWrapper.useTitleFilter()) {
            return
        }*/

        if (keyword.isNullOrBlank()) {
            all()
            return
        }

        query(
            Maybe.fromCallable { articleRepository.search("${tokenizer(keyword, 2)}") }
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.computation())
                .flatMapObservable { it.toObservable() }
        )
    }

    private fun query(results: Observable<SearchResult>) {
        adapter.clear()
        setSearchStart()

        val start = System.currentTimeMillis()
        results
            .doOnTerminate { setSearchEnded(System.currentTimeMillis() - start) }
            .subscribe(
                adapter::add,
                {
                    Timber.e(it)
                    progressCallback.hideProgress()
                },
                { setSearchEnded(System.currentTimeMillis() - start) }
            )
            .addTo(disposables)
    }

    private fun setSearchStart() {
        progressCallback.showProgress()
        progressCallback.setProgressMessage(getString(R.string.message_search_in_progress))
    }

    @UiThread
    private fun setSearchEnded(duration: Long) {
        Completable.fromAction {
            progressCallback.hideProgress()
            adapter.notifyDataSetChanged()
            progressCallback.setProgressMessage("${adapter.itemCount} Articles / $duration[ms]")
        }
            .subscribeOn(AndroidSchedulers.mainThread())
            .subscribe()
            .addTo(disposables)
    }

    override fun onCreateOptionsMenu(menu: Menu, menuInflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, menuInflater)
        menuInflater.inflate(R.menu.menu_article_list, menu)
        // TODO menu.findItem(R.id.action_switch_title_filter)?.isChecked = preferencesWrapper.useTitleFilter()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_all_article -> {
                all()
                true
            }
            R.id.action_set_target -> {
                val intent = Intent(Intent.ACTION_GET_CONTENT)
                intent.addCategory(Intent.CATEGORY_OPENABLE)
                intent.type = "application/zip"
                startActivityForResult(intent, 1)
                true
            }
            R.id.action_to_top -> {
                RecyclerViewScroller.toTop(binding.results, adapter.itemCount)
                true
            }
            R.id.action_to_bottom -> {
                RecyclerViewScroller.toBottom(binding.results, adapter.itemCount)
                true
            }
            R.id.action_switch_title_filter -> {
                val newState = !item.isChecked
                // TODO preferencesWrapper.switchUseTitleFilter(newState)
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

        /*
        val file = File(FileExtractorFromUri(requireContext(), target.toUri()))
        if (preferencesWrapper.getLastUpdated() == file.lastModified()) {
            articleListFragment.all()
            return
        }*/

        showProgress()

        ZipLoaderService.start(requireContext(), target)
    }

    override fun showProgress() {
        binding.progressCircular.progress = 0
        binding.progressCircular.visibility = View.VISIBLE
    }

    override fun hideProgress() {
        binding.progressCircular.visibility = View.GONE
    }

    override fun setProgressMessage(message: String) {
        appBarBinding.searchResult.text = message
    }

    override fun onDestroy() {
        super.onDestroy()
        disposables.clear()
        context?.unregisterReceiver(progressBroadcastReceiver)
    }
}
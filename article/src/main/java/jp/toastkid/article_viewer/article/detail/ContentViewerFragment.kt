/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.article_viewer.article.detail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.annotation.UiThread
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.halilibo.richtext.markdown.Markdown
import com.halilibo.richtext.ui.RichText
import jp.toastkid.article_viewer.R
import jp.toastkid.article_viewer.article.ArticleRepository
import jp.toastkid.article_viewer.article.data.AppDatabase
import jp.toastkid.article_viewer.article.detail.viewmodel.ContentViewerFragmentViewModel
import jp.toastkid.article_viewer.bookmark.Bookmark
import jp.toastkid.lib.AppBarViewModel
import jp.toastkid.lib.BrowserViewModel
import jp.toastkid.lib.ContentScrollable
import jp.toastkid.lib.ContentViewModel
import jp.toastkid.lib.TabListViewModel
import jp.toastkid.lib.preference.PreferenceApplier
import jp.toastkid.lib.scroll.rememberViewInteropNestedScrollConnection
import jp.toastkid.lib.tab.OnBackCloseableTabUiFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * @author toastkidjp
 */
class ContentViewerFragment : Fragment(), ContentScrollable, OnBackCloseableTabUiFragment {

    private lateinit var repository: ArticleRepository

    private lateinit var viewModel: ContentViewerFragmentViewModel

    private val subheads = mutableListOf<String>()

    private var scrollState: ScrollState? = null

    private val disposables = Job()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val activity = activity
            ?: return super.onCreateView(inflater, container, savedInstanceState)

        repository = AppDatabase.find(activity).articleRepository()

        /*TODO ContextMenuInitializer(
            binding.content,
            ViewModelProvider(it).get(BrowserViewModel::class.java)
        ).invoke()*/

        viewModel = ViewModelProvider(this).get(ContentViewerFragmentViewModel::class.java)

        setHasOptionsMenu(true)

        val composeView = ComposeView(activity)
        composeView.setViewCompositionStrategy(
            ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed
        )
        composeView.setContent {
            val listState = rememberScrollState()
            this.scrollState = listState
            ContentViewerUi(listState)
        }

        val appBarComposeView = ComposeView(activity)
        appBarComposeView.setViewCompositionStrategy(
            ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed
        )
        appBarComposeView.setContent {
            AppBarContent()
        }
        ViewModelProvider(activity).get(AppBarViewModel::class.java)
            .replace(appBarComposeView)

        return composeView
    }

    @Composable
    fun ContentViewerUi(scrollState: ScrollState) {
        /*
        binding.contentScroll.setBackgroundColor(preferenceApplier.editorBackgroundColor())

        val editorFontColor = preferenceApplier.editorFontColor()
        binding.content.setTextColor(editorFontColor)
        binding.content.setLinkTextColor(LinkColorGenerator().invoke(editorFontColor))
        binding.content.highlightColor = preferenceApplier.editorHighlightColor(Color.CYAN)*/
        val context = context ?: return
        val preferenceApplier = PreferenceApplier(context)
        val linkBehaviorService = makeLinkBehaviorService()

        MaterialTheme {
            Surface(modifier = Modifier.background(Color.Transparent)) {
                RichText(
                    modifier = Modifier
                        .padding(start = 16.dp, end = 16.dp)
                        .verticalScroll(scrollState)
                        .nestedScroll(rememberViewInteropNestedScrollConnection()),
                        //.background(Color(preferenceApplier.editorBackgroundColor())),
                ) {
                    Markdown(
                        viewModel.content.value,
                        onLinkClicked = {
                            linkBehaviorService?.invoke(it)
                        }
                    )
                }
            }
        }
    }

    private fun makeLinkBehaviorService(): LinkBehaviorService? {
        val activity = activity ?: return null
        val viewModelProvider = ViewModelProvider(activity)
        return LinkBehaviorService(
                viewModelProvider.get(ContentViewModel::class.java),
                viewModelProvider.get(BrowserViewModel::class.java),
                { repository.exists(it) > 0 }
        )
    }

    @Preview
    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    fun AppBarContent() {
        val activityContext = activity ?: return
        val preferenceApplier = PreferenceApplier(activityContext)

        val activity = activity ?: return
        val tabListViewModel = ViewModelProvider(activity).get(TabListViewModel::class.java)

        var searchInput by remember { mutableStateOf("") }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier =  Modifier
                .height(56.dp)
                .fillMaxWidth()
        ) {
            Column(Modifier.weight(1f)) {
                TextField(
                    value = searchInput,
                    onValueChange = {
                        searchInput = it
                    },
                    label = { viewModel.title.value },
                    singleLine = true,
                    keyboardActions = KeyboardActions{
                        //TODO contentTextSearchUseCase.invoke(it.toString())
                    },
                    colors = TextFieldDefaults.textFieldColors(
                        textColor = Color(preferenceApplier.fontColor),
                        unfocusedLabelColor = Color(preferenceApplier.fontColor),
                        focusedIndicatorColor = Color(preferenceApplier.fontColor)
                    ),
                    trailingIcon = {
                        Icon(
                        Icons.Filled.Clear,
                        tint = Color(preferenceApplier.fontColor),
                        contentDescription = "clear text",
                        modifier = Modifier
                            .offset(x = 8.dp)
                            .clickable {
                                searchInput = ""
                            }
                    )
                    }
                )
            }
            Box(
                Modifier
                    .width(40.dp)
                    .fillMaxHeight()
                    .combinedClickable(
                    true,
                    onClick = {
                        tabList()
                    },
                    onLongClick = {
                        tabListViewModel.openNewTabForLongTap()
                    }
                )
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_tab), 
                    contentDescription = stringResource(id = R.string.tab),
                    colorFilter = ColorFilter.tint(
                        Color(preferenceApplier.fontColor),
                        BlendMode.SrcIn
                    ),
                    modifier = Modifier.align(Alignment.Center)
                )
                Text(
                    text = tabListViewModel.tabCount.value.toString(),
                    fontSize = 9.sp,
                    color = Color(preferenceApplier.fontColor),
                    modifier = Modifier.align(Alignment.Center)
                        .padding(start = 2.dp, bottom = 2.dp)
                )
            }
        }
    }

    @UiThread
    fun loadContent(title: String) {
        viewModel.setTitle(title)

        CoroutineScope(Dispatchers.IO).launch(disposables) {
            val content = repository.findContentByTitle(title)
            if (content.isNullOrBlank()) {
                return@launch
            }

            withContext(Dispatchers.Main) {
                viewModel.setContent(content)
            }
        }
    }

    fun tabList() {
        activity?.let {
            ViewModelProvider(it).get(ContentViewModel::class.java).switchTabList()
        }
    }

    fun showSubheads() {
        if (subheads.isEmpty()) {
            return
        }

        /*TODO SubheadDialogFragment.make(subheads)
            .show(parentFragmentManager, SubheadDialogFragment::class.java.canonicalName)*/
    }

    override fun toTop() {
        CoroutineScope(Dispatchers.Main).launch {
            scrollState?.scrollTo(0)
        }
    }

    override fun toBottom() {
        CoroutineScope(Dispatchers.Main).launch {
            scrollState?.scrollTo(scrollState?.maxValue ?: 0)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_content_viewer, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_add_to_bookmark -> {
                CoroutineScope(Dispatchers.IO).launch {
                    val title = viewModel.title.value
                    val article = repository.findFirst(title) ?: return@launch
                    context?.let {
                        AppDatabase.find(it)
                            .bookmarkRepository()
                            .add(Bookmark(article.id))
                    }
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onDetach() {
        disposables.cancel()
        super.onDetach()
    }

}
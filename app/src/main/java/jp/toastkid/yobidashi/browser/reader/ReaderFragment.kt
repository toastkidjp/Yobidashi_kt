/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.browser.reader

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.Image
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter.Companion.tint
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import jp.toastkid.lib.ContentScrollable
import jp.toastkid.lib.preference.PreferenceApplier
import jp.toastkid.lib.viewmodel.PageSearcherViewModel
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.libs.speech.SpeechMaker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * @author toastkidjp
 */
class ReaderFragment : Fragment(), ContentScrollable {

    private lateinit var speechMaker: SpeechMaker

    private var scrollState: ScrollState? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        val context = activity ?: return super.onCreateView(inflater, container, savedInstanceState)

        speechMaker = SpeechMaker(context)

        val composeView = ComposeView(context)
        composeView.setViewCompositionStrategy(
            ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed
        )
        val viewModelProvider = ViewModelProvider(context)

        viewModelProvider.get(ReaderFragmentViewModel::class.java)
            .content
            .observe(viewLifecycleOwner, {
                composeView.setContent {
                    ReaderUi(it.first, it.second)
                }
            })

        // TODO val finder = TextViewHighlighter(binding.textContent)
        viewModelProvider.get(PageSearcherViewModel::class.java)
            .find
            .observe(viewLifecycleOwner, Observer {
                //TODO finder(it)
            })

        setHasOptionsMenu(true)
        return composeView
    }

    @Composable
    fun ReaderUi(title: String, text: String) {
        val preferenceApplier = PreferenceApplier(LocalContext.current)

        val scrollState = rememberScrollState()
        this.scrollState = scrollState

        MaterialTheme() {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .background(Color(preferenceApplier.editorBackgroundColor()))
                    .padding(16.dp)
            ) {
                Column(Modifier.verticalScroll(scrollState)) {
                    Text(
                        text = title,
                        color = Color(preferenceApplier.editorFontColor()),
                        fontSize = 30.sp,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        text = text,
                        color = Color(preferenceApplier.editorFontColor()),
                        fontSize = 16.sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight()
                    )
                }
                Image(
                    painterResource(R.drawable.ic_close_black),
                    contentDescription = stringResource(id = R.string.close),
                    colorFilter = tint(Color(preferenceApplier.fontColor), BlendMode.SrcIn),
                    modifier = Modifier
                        .width(60.dp)
                        .height(60.dp)
                        .padding(16.dp)
                        .align(Alignment.TopEnd)
                        .clickable {
                            activity?.supportFragmentManager?.popBackStack()
                        }
                )
            }
        }

        /*
        binding.textContent.customSelectionActionModeCallback = object : ActionMode.Callback {

            override fun onCreateActionMode(actionMode: ActionMode?, menu: Menu?): Boolean {
                val text = extractSelectedText()
                if (Urls.isValidUrl(text)) {
                    MenuInflater(context).inflate(R.menu.context_editor_url, menu)
                }
                MenuInflater(context).inflate(R.menu.context_speech, menu)
                return true
            }

            override fun onActionItemClicked(
                actionMode: ActionMode?,
                menuItem: MenuItem?
            ): Boolean {
                val text = extractSelectedText()
                when (menuItem?.itemId) {
                    R.id.context_edit_speech -> {
                        speechMaker.invoke(text)
                        actionMode?.finish()
                        return true
                    }
                    else -> Unit
                }
                actionMode?.finish()
                return false
            }

            private fun extractSelectedText(): String {
                return binding.textContent.text
                    .subSequence(
                        binding.textContent.selectionStart,
                        binding.textContent.selectionEnd
                    )
                    .toString()
            }

            override fun onPrepareActionMode(p0: ActionMode?, p1: Menu?) = true

            override fun onDestroyActionMode(p0: ActionMode?) = Unit

        }
         */
    }

    fun close() {
        activity?.supportFragmentManager?.popBackStack()
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
        inflater.inflate(R.menu.reader, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_speech -> {
                val activity = activity ?: return true
                val viewModelProvider = ViewModelProvider(activity)

                val pair = viewModelProvider.get(ReaderFragmentViewModel::class.java)
                    .content
                    .value ?: return true
                speechMaker.invoke("${pair.first} ${pair.second}")
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onPause() {
        super.onPause()
        speechMaker.stop()
    }

    override fun onDetach() {
        speechMaker.dispose()
        super.onDetach()
    }

}
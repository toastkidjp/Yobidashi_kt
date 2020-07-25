/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.browser.reader

import android.os.Bundle
import android.view.ActionMode
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import jp.toastkid.lib.ContentScrollable
import jp.toastkid.lib.Urls
import jp.toastkid.lib.preference.PreferenceApplier
import jp.toastkid.lib.view.TextViewHighlighter
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.browser.page_search.PageSearcherViewModel
import jp.toastkid.yobidashi.databinding.FragmentReaderModeBinding
import jp.toastkid.yobidashi.libs.speech.SpeechMaker

/**
 * @author toastkidjp
 */
class ReaderFragment : Fragment(), ContentScrollable {

    private lateinit var binding: FragmentReaderModeBinding

    private lateinit var speechMaker: SpeechMaker

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        binding = DataBindingUtil.inflate(inflater, LAYOUT_ID, container, false)
        binding.fragment = this
        speechMaker = SpeechMaker(binding.root.context)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.textContent.customSelectionActionModeCallback = object : ActionMode.Callback {

            override fun onCreateActionMode(actionMode: ActionMode?, menu: Menu?): Boolean {
                val text = extractSelectedText()
                if (Urls.isValidUrl(text)) {
                    MenuInflater(context).inflate(R.menu.context_editor_url, menu)
                }
                MenuInflater(context).inflate(R.menu.context_speech, menu)
                return true
            }

            override fun onActionItemClicked(actionMode: ActionMode?, menuItem: MenuItem?): Boolean {
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
                        .subSequence(binding.textContent.selectionStart, binding.textContent.selectionEnd)
                        .toString()
            }

            override fun onPrepareActionMode(p0: ActionMode?, p1: Menu?) = true

            override fun onDestroyActionMode(p0: ActionMode?) = Unit

        }

        activity?.also { activity ->
            val finder = TextViewHighlighter(binding.textContent)

            ViewModelProvider(activity)
                    .get(PageSearcherViewModel::class.java)
                    .find.observe(activity, Observer { finder(it ?: "") })

            ViewModelProvider(activity).get(ReaderFragmentViewModel::class.java)
                    .content
                    .observe(activity, Observer { setContent(it.first, it.second) })
        }

        setHasOptionsMenu(true)
    }

    private fun setContent(title: String, content: String) {
        binding.title.text = title
        binding.textContent.text = content
    }

    fun close() {
        activity?.supportFragmentManager?.popBackStack()
    }

    override fun toTop() {
        binding.scroll.smoothScrollTo(0, 0)
    }

    override fun toBottom() {
        binding.scroll.smoothScrollTo(0, binding.textContent.measuredHeight)
    }

    override fun onResume() {
        super.onResume()

        val preferenceApplier = PreferenceApplier(binding.root.context)
        binding.background.setBackgroundColor(preferenceApplier.editorBackgroundColor())

        val editorFontColor = preferenceApplier.editorFontColor()
        binding.title.setTextColor(editorFontColor)
        binding.textContent.setTextColor(editorFontColor)
        binding.close.setColorFilter(editorFontColor)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.reader, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_speech -> {
                speechMaker.invoke("${binding.title.text}$lineSeparator${binding.textContent.text}")
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

    companion object {

        @LayoutRes
        private val LAYOUT_ID = R.layout.fragment_reader_mode

        private val lineSeparator = System.lineSeparator()

    }

}
/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.browser.reader

import android.os.Bundle
import android.view.*
import androidx.annotation.LayoutRes
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.databinding.FragmentReaderModeBinding
import jp.toastkid.yobidashi.libs.Urls
import jp.toastkid.yobidashi.libs.preference.PreferenceApplier
import jp.toastkid.yobidashi.libs.speech.SpeechMaker

/**
 * @author toastkidjp
 */
class ReaderFragment : Fragment() {

    private lateinit var binding: FragmentReaderModeBinding

    private lateinit var speechMaker: SpeechMaker

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        binding = DataBindingUtil.inflate(inflater, LAYOUT_ID, container, false)
        speechMaker = SpeechMaker(binding.root.context)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        arguments?.let { arguments ->
            arguments.getString(KEY_TITLE)?.also { binding.title.text = it }
            arguments.getString(KEY_CONTENT)?.also { binding.content.text = it }
        }

        binding.content.customSelectionActionModeCallback = object : ActionMode.Callback {

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
                return binding.content.text
                        .subSequence(binding.content.selectionStart, binding.content.selectionEnd)
                        .toString()
            }

            override fun onPrepareActionMode(p0: ActionMode?, p1: Menu?) = true

            override fun onDestroyActionMode(p0: ActionMode?) = Unit

        }

        binding.close.setOnClickListener {
            close()
        }

        setHasOptionsMenu(true)
    }

    fun close() {
        ViewModelProviders.of(requireActivity())[ReaderFragmentViewModel::class.java].close()
    }

    override fun onResume() {
        super.onResume()

        val preferenceApplier = PreferenceApplier(binding.root.context)
        binding.background.setBackgroundColor(preferenceApplier.editorBackgroundColor())

        val editorFontColor = preferenceApplier.editorFontColor()
        binding.title.setTextColor(editorFontColor)
        binding.content.setTextColor(editorFontColor)
        binding.close.setColorFilter(editorFontColor)
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater?.inflate(R.menu.reader, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.action_speech -> {
                speechMaker.invoke("${binding.title.text}$lineSeparator${binding.content.text}")
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onPause() {
        super.onPause()
        speechMaker.stop()
    }

    override fun onDestroy() {
        super.onDestroy()
        speechMaker.dispose()
    }

    companion object {

        @LayoutRes
        private val LAYOUT_ID = R.layout.fragment_reader_mode

        private const val KEY_TITLE = "title"

        private const val KEY_CONTENT = "content"

        private val lineSeparator = System.getProperty("line.separator")

        fun withContent(title: String, content: String): ReaderFragment {
            val readerFragment = ReaderFragment()
            readerFragment.arguments = bundleOf(
                    KEY_TITLE to title,
                    KEY_CONTENT to content
            )
            return readerFragment
        }
    }

}
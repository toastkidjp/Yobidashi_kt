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
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.databinding.FragmentReaderModeBinding

/**
 * @author toastkidjp
 */
class ReaderFragment : Fragment() {

    private lateinit var binding: FragmentReaderModeBinding

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        binding = DataBindingUtil.inflate(inflater, LAYOUT_ID, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        arguments?.let { arguments ->
            arguments.getString(KEY_TITLE)?.also { binding.title.text = it }
            arguments.getString(KEY_CONTENT)?.also { binding.content.text = it }
        }


        binding.close.setOnClickListener {
            ViewModelProviders.of(requireActivity())[ReaderFragmentViewModel::class.java].close()
        }
    }

    companion object {

        @LayoutRes
        private val LAYOUT_ID = R.layout.fragment_reader_mode

        private const val KEY_TITLE = "title"

        private const val KEY_CONTENT = "content"

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
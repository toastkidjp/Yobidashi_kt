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
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_reader_mode, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        arguments?.getString("title")?.also { binding.title.text = it }
        arguments?.getString("content")?.also { binding.content.text = it }

        binding.close.setOnClickListener {
            ViewModelProviders.of(requireActivity())[ReaderFragmentViewModel::class.java].close()
            //fragmentManager?.beginTransaction()?.remove(this)?.commit()
        }
    }

    companion object {

        fun withContent(title: String, content: String): ReaderFragment {
            val readerFragment = ReaderFragment()
            readerFragment.arguments = bundleOf(
                    "title" to title,
                    "content" to content
            )
            return readerFragment
        }
    }

}
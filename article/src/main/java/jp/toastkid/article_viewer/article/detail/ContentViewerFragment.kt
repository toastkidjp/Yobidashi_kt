/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.article_viewer.article.detail

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.widget.addTextChangedListener
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import jp.toastkid.article_viewer.common.ProgressCallback
import jp.toastkid.article_viewer.common.SearchFunction
import jp.toastkid.article_viewer.R
import jp.toastkid.article_viewer.databinding.AppBarArticleListBinding
import jp.toastkid.article_viewer.databinding.FragmentContentBinding
import jp.toastkid.lib.view.TextViewHighlighter

/**
 * @author toastkidjp
 */
class ContentViewerFragment : Fragment(), SearchFunction {

    private lateinit var binding: FragmentContentBinding

    private lateinit var appBarBinding: AppBarArticleListBinding

    private lateinit var textViewHighlighter: TextViewHighlighter

    private var progressCallback: ProgressCallback? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)

        if (context is ProgressCallback) {
            progressCallback = context
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_content, container, false)
        appBarBinding = DataBindingUtil.inflate(inflater, R.layout.app_bar_article_list, container, false)
        textViewHighlighter = TextViewHighlighter(binding.content)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        binding.content.text = arguments?.getString("content")
        arguments?.getString("title")?.also {
            appBarBinding.searchResult.text = it
        }

        appBarBinding.input.addTextChangedListener {
            search(it.toString())
        }
    }

    override fun search(keyword: String?) {
        textViewHighlighter(keyword)
    }

    override fun filter(keyword: String?) = Unit

    companion object {
        fun make(title: String, content: String): Fragment
                = ContentViewerFragment().also {
                    it.arguments = bundleOf(
                        "content" to content,
                        "title" to title
                    )
                }
    }
}
/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.article_viewer.article.detail

import android.os.Bundle
import android.text.util.Linkify
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.widget.addTextChangedListener
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import jp.toastkid.article_viewer.R
import jp.toastkid.article_viewer.article.data.AppDatabase
import jp.toastkid.article_viewer.common.SearchFunction
import jp.toastkid.article_viewer.databinding.AppBarArticleListBinding
import jp.toastkid.article_viewer.databinding.FragmentContentBinding
import jp.toastkid.lib.BrowserViewModel
import jp.toastkid.lib.ContentScrollable
import jp.toastkid.lib.ContentViewModel
import jp.toastkid.lib.view.TextViewHighlighter
import kotlinx.coroutines.Job
import java.util.regex.Pattern

/**
 * @author toastkidjp
 */
class ContentViewerFragment : Fragment(), SearchFunction, ContentScrollable {

    private lateinit var binding: FragmentContentBinding

    private lateinit var appBarBinding: AppBarArticleListBinding

    private lateinit var textViewHighlighter: TextViewHighlighter

    private val disposables = Job()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_content, container, false)
        appBarBinding = DataBindingUtil.inflate(inflater, R.layout.app_bar_article_list, container, false)
        textViewHighlighter = TextViewHighlighter(binding.content)

        val linkBehaviorService = makeLinkBehaviorService()

        val linkMovementMethod = ContentLinkMovementMethod { url ->
            linkBehaviorService.invoke(url)
        }
        binding.content.movementMethod = linkMovementMethod
        return binding.root
    }

    private fun makeLinkBehaviorService(): LinkBehaviorService {
        val repository = AppDatabase.find(requireContext()).diaryRepository()
        val viewModelProvider = ViewModelProvider(requireActivity())
        val linkBehaviorService = LinkBehaviorService(
                repository,
                viewModelProvider.get(ContentViewModel::class.java),
                viewModelProvider.get(BrowserViewModel::class.java)
        )
        return linkBehaviorService
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

        binding.content.linksClickable = true

        embedLinks(
                internalLinkPattern,
                Linkify.TransformFilter { matcher, s ->
                    InternalLinkScheme.makeLink(matcher.group(1))
                }
        )

        embedLinks(
                httpPattern,
                Linkify.TransformFilter { matcher, s -> matcher.group(0) }
        )
    }

    private fun embedLinks(pattern: Pattern, transformFilter: Linkify.TransformFilter) {
        Linkify.addLinks(binding.content, pattern, null, null, transformFilter)
    }

    override fun search(keyword: String?) {
        textViewHighlighter(keyword)
    }

    override fun filter(keyword: String?) = Unit

    override fun toTop() {
        binding.contentScroll.smoothScrollTo(0, 0)
    }

    override fun toBottom() {
        binding.contentScroll.smoothScrollTo(0, binding.content.measuredHeight)
    }

    override fun onDetach() {
        disposables.cancel()
        super.onDetach()
    }

    companion object {

        private val internalLinkPattern =
                Pattern.compile("\\[\\[(.+?)\\]\\]", Pattern.DOTALL)

        private val httpPattern =
                Pattern.compile("https?://[a-zA-Z0-9/:%#&~=_!'\\\\\$\\\\?\\\\.\\\\+\\\\*\\\\-]+")

        fun make(title: String, content: String): Fragment
                = ContentViewerFragment().also {
                    it.arguments = bundleOf(
                        "content" to content,
                        "title" to title
                    )
                }
    }
}
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
import android.text.Spannable
import android.text.method.LinkMovementMethod
import android.text.util.Linkify
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.net.toUri
import androidx.core.os.bundleOf
import androidx.core.widget.addTextChangedListener
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import jp.toastkid.article_viewer.R
import jp.toastkid.article_viewer.article.data.AppDatabase
import jp.toastkid.article_viewer.common.ProgressCallback
import jp.toastkid.article_viewer.common.SearchFunction
import jp.toastkid.article_viewer.databinding.AppBarArticleListBinding
import jp.toastkid.article_viewer.databinding.FragmentContentBinding
import jp.toastkid.lib.BrowserViewModel
import jp.toastkid.lib.ContentScrollable
import jp.toastkid.lib.ContentViewModel
import jp.toastkid.lib.view.TextViewHighlighter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.util.regex.Pattern

/**
 * @author toastkidjp
 */
class ContentViewerFragment : Fragment(), SearchFunction, ContentScrollable {

    private lateinit var binding: FragmentContentBinding

    private lateinit var appBarBinding: AppBarArticleListBinding

    private lateinit var textViewHighlighter: TextViewHighlighter

    // TODO Delete it.
    private var progressCallback: ProgressCallback? = null

    private val disposables = Job()

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

        val repository = AppDatabase.find(requireContext()).diaryRepository()

        val linkMovementMethod = ContentLinkMovementMethod { url ->
            if (url.isNullOrBlank()) {
                return@ContentLinkMovementMethod
            }

            val viewModelProvider = ViewModelProvider(requireActivity())

            if (url.startsWith("internal-article://")) {
                val title = url.substring("internal-article://".length)
                CoroutineScope(Dispatchers.Main).launch(disposables) {
                    val content = withContext(Dispatchers.IO) {
                        repository.findContentByTitle(title)
                    }
                    if (content.isNullOrBlank()) {
                        return@launch
                    }
                    viewModelProvider.get(ContentViewModel::class.java)
                            .nextFragment(make(title, content))
                }
                return@ContentLinkMovementMethod
            }

            viewModelProvider.get(BrowserViewModel::class.java)
                    .open(url.toUri())
        }
        binding.content.movementMethod = linkMovementMethod
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

        binding.content.linksClickable = true
        val pattern = Pattern.compile("\\[\\[(.+?)\\]\\]", Pattern.DOTALL)
        val scheme = "internal-article"

        Linkify.addLinks(
                binding.content,
                pattern,
                null,
                null,
                { matcher, s -> "$scheme://${matcher.group(1)}" }
        )

        val httpPattern = Pattern.compile("https?://[a-zA-Z0-9/:%#&~=_!'\\\\\$\\\\?\\\\.\\\\+\\\\*\\\\-]+")
        Linkify.addLinks(
                binding.content,
                httpPattern,
                null,
                null,
                { matcher, s -> matcher.group(0) }
        )
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
        fun make(title: String, content: String): Fragment
                = ContentViewerFragment().also {
                    it.arguments = bundleOf(
                        "content" to content,
                        "title" to title
                    )
                }
    }
}
/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.search.clip

import android.support.annotation.ColorInt
import android.support.v4.content.ContextCompat
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.databinding.ModuleSearchClipboardBinding
import jp.toastkid.yobidashi.libs.Urls
import jp.toastkid.yobidashi.libs.clip.Clipboard
import jp.toastkid.yobidashi.libs.facade.BaseModule

/**
 * Search module using clipboard content.
 *
 * @param binding [ModuleSearchClipboardBinding]
 * @param onClick callback
 *
 * @author toastkidjp
 */
class ClipboardModule(
        private val binding: ModuleSearchClipboardBinding,
        onClick: (String) -> Unit
) : BaseModule(binding.root) {

    /**
     * Link color.
     */
    @ColorInt
    private val linkColor = ContextCompat.getColor(binding.root.context, R.color.link_blue)

    /**
     * Text color.
     */
    @ColorInt
    private val textColor = ContextCompat.getColor(binding.root.context, R.color.black)

    init {
        binding.root.setOnClickListener {
            val activityContext = it.context
            onClick(binding.text.text.toString())
            Clipboard.clip(activityContext, "")
        }
    }

    /**
     * Switch content.
     */
    fun switch() {
        val primary = Clipboard.getPrimary(binding.root.context)?.toString()
        if (primary == null || primary.isBlank() || primary.length > CONTENT_LENGTH_LIMIT) {
            hide()
            return
        }

        show()
        if (Urls.isValidUrl(primary)) {
            setLink(primary)
        } else {
            setSearch(primary)
        }
    }

    /**
     * Set search query and icon.
     *
     * @param query Query string
     */
    private fun setSearch(query: String) {
        binding.image.setImageResource(R.drawable.ic_search)
        binding.text.text = query
        binding.text.setTextColor(textColor)
    }

    /**
     * Set open link and icon.
     *
     * @param link Link URL(string)
     */
    private fun setLink(link: String) {
        binding.image.setImageResource(R.drawable.ic_web_black)
        binding.text.text = link
        binding.text.setTextColor(linkColor)
    }

    companion object {

        /**
         * Content length limit.
         */
        private const val CONTENT_LENGTH_LIMIT = 50

    }
}
/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.article_viewer.article.detail

import android.view.ActionMode
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.TextView
import androidx.core.net.toUri
import jp.toastkid.article_viewer.R
import jp.toastkid.lib.BrowserViewModel
import jp.toastkid.lib.Urls

/**
 * @author toastkidjp
 */
class ContextMenuInitializer(
        private val textView: TextView,
        private val browserViewModel: BrowserViewModel
) {

    operator fun invoke() {
        val context = textView.context

        textView.customSelectionActionModeCallback = object : ActionMode.Callback {

            override fun onCreateActionMode(actionMode: ActionMode?, menu: Menu?): Boolean {
                val text = extractSelectedText()
                if (Urls.isValidUrl(text)) {
                    MenuInflater(context).inflate(R.menu.context_article_content_url, menu)
                }
                return true
            }

            override fun onActionItemClicked(actionMode: ActionMode?, menuItem: MenuItem?): Boolean {
                val text = extractSelectedText()
                when (menuItem?.itemId) {
                    R.id.context_article_content_url_open_new -> {
                        browserViewModel.open(text.toUri())
                        actionMode?.finish()
                        return true
                    }
                    R.id.context_article_content_url_open_background -> {
                        browserViewModel.openBackground(text.toUri())
                        actionMode?.finish()
                        return true
                    }
                    R.id.context_article_content_url_preview -> {
                        browserViewModel.preview(text.toUri())
                        actionMode?.finish()
                        return true
                    }
                    else -> Unit
                }
                return false
            }

            private fun extractSelectedText(): String {
                return textView.text
                        .subSequence(textView.selectionStart, textView.selectionEnd)
                        .toString()
            }

            override fun onPrepareActionMode(p0: ActionMode?, p1: Menu?) = true

            override fun onDestroyActionMode(p0: ActionMode?) = Unit

        }
    }
}
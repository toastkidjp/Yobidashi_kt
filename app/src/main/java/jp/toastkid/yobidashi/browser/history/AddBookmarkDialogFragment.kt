/*
 * Copyright (c) 2018 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.browser.history

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.browser.bookmark.BookmarkInsertion
import jp.toastkid.yobidashi.browser.bookmark.Bookmarks
import jp.toastkid.yobidashi.browser.bookmark.model.Bookmark

/**
 * @author toastkidjp
 */
class AddBookmarkDialogFragment : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val activityContext = context ?: return super.onCreateDialog(savedInstanceState)

        val title = arguments?.getString(KEY_TITLE) ?: ""
        val url = arguments?.getString(KEY_URL) ?: ""

        return AlertDialog.Builder(activityContext)
                .setTitle(R.string.title_add_bookmark)
                .setMessage(R.string.message_add_bookmark)
                .setPositiveButton(R.string.ok) { d, _ ->
                    BookmarkInsertion(
                        activityContext,
                        title,
                        url,
                        Bookmarks.makeFaviconUrl(activityContext, url),
                        Bookmark.getRootFolderName(),
                        false
                    ).insert()
                    d.dismiss()
                }
                .setNegativeButton(R.string.cancel) { d, _ -> d.cancel() }
                .create()
    }

    companion object {
        private const val KEY_TITLE = "title"

        private const val KEY_URL = "url"

        fun make(title: String, url: String) =
                AddBookmarkDialogFragment().also {
                    it.arguments = bundleOf(
                            KEY_TITLE to title,
                            KEY_URL to url
                    )
                }
    }
}
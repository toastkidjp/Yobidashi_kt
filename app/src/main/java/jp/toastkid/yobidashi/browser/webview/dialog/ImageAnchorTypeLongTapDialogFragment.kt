/*
 * Copyright (c) 2018 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.browser.webview.dialog

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.core.net.toUri
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import jp.toastkid.lib.BrowserViewModel
import jp.toastkid.lib.Urls
import jp.toastkid.lib.preference.PreferenceApplier
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.browser.ImageDownloadActionDialogFragment
import jp.toastkid.yobidashi.libs.Toaster
import jp.toastkid.yobidashi.libs.clip.Clipboard

/**
 * TODO Rename url to imageURL.
 * TODO Add image search
 * @author toastkidjp
 */
class ImageAnchorTypeLongTapDialogFragment : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val activityContext = context ?: return super.onCreateDialog(savedInstanceState)

        val title = arguments?.getString(KEY_TITLE) ?: ""

        val url = arguments?.getString(KEY_EXTRA)
                ?: return super.onCreateDialog(savedInstanceState)

        val anchor = arguments?.getString(KEY_ANCHOR)
                ?: return super.onCreateDialog(savedInstanceState)

        val viewModel = ViewModelProvider(requireActivity()).get(BrowserViewModel::class.java)

        val uri = anchor.toUri()

        return AlertDialog.Builder(activityContext)
                .setTitle("URL: $url")
                .setItems(R.array.image_anchor_menu) { _, which ->
                    when (which) {
                        0 -> viewModel.open(uri)
                        1 -> viewModel.openBackground(title, uri)
                        2 -> viewModel.open("https://www.google.co.jp/searchbyimage?image_url=$url".toUri())
                        3 -> ViewModelProvider(requireActivity()).get(BrowserViewModel::class.java).preview(uri)
                        4 -> downloadImage(url)
                        5 -> Clipboard.clip(activityContext, anchor)
                    }
                }
                .setNegativeButton(R.string.cancel) { d, _ -> d.cancel() }
                .create()
    }

    private fun downloadImage(url: String) {
        val activityContext = context ?: return
        if (Urls.isInvalidUrl(url)) {
            Toaster.snackShort(
                    requireActivity().findViewById(android.R.id.content),
                    activityContext.getString(R.string.message_cannot_downloading_image),
                    PreferenceApplier(activityContext).colorPair()
            )
            return
        }

        ImageDownloadActionDialogFragment.show(activityContext, url)
    }

    companion object {

        private const val KEY_TITLE = "title"

        private const val KEY_ANCHOR = "anchor"

        private const val KEY_EXTRA = "extra"

        fun make(title: String, extra: String, anchor: String) =
                ImageAnchorTypeLongTapDialogFragment()
                        .also {
                            it.arguments = bundleOf(
                                    KEY_TITLE to title,
                                    KEY_EXTRA to extra,
                                    KEY_ANCHOR to anchor
                            )
                        }
    }
}
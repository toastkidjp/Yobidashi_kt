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
import androidx.lifecycle.ViewModelStoreOwner
import jp.toastkid.lib.BrowserViewModel
import jp.toastkid.lib.ContentViewModel
import jp.toastkid.lib.Urls
import jp.toastkid.search.ImageSearchUrlGenerator
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.browser.ImageDownloadActionDialogFragment

/**
 * @author toastkidjp
 */
class ImageTypeLongTapDialogFragment : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val activityContext = activity ?: return super.onCreateDialog(savedInstanceState)

        val url = arguments?.getString(KEY_EXTRA)
                ?: return super.onCreateDialog(savedInstanceState)

        val viewModelProvider = ViewModelProvider(activityContext)

        val viewModel = viewModelProvider.get(BrowserViewModel::class.java)

        return AlertDialog.Builder(activityContext)
                .setTitle("Image: $url")
                .setItems(R.array.image_menu) { _, which ->
                    when (which) {
                        0 -> viewModel.open(ImageSearchUrlGenerator()(url))
                        1 -> viewModel.preview(url.toUri())
                        2 -> downloadImage(url)
                    }
                }
            .setNegativeButton(R.string.cancel) { d, _ -> d.cancel() }
                .create()
    }

    private fun downloadImage(url: String) {
        val activityContext = activity ?: return
        if (Urls.isInvalidUrl(url)) {
            val contentViewModel = (context as? ViewModelStoreOwner)?.let {
                ViewModelProvider(it).get(ContentViewModel::class.java)
            }

            contentViewModel?.snackShort(R.string.message_cannot_downloading_image)
            return
        }

        ImageDownloadActionDialogFragment.show(activityContext, url)
    }

    companion object {

        private const val KEY_EXTRA = "extra"

        fun make(extra: String) =
                ImageTypeLongTapDialogFragment()
                        .also { it.arguments = bundleOf(KEY_EXTRA to extra) }
    }
}
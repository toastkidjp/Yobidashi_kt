/*
 * Copyright (c) 2022 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.about.view

import android.app.Dialog
import android.os.Bundle
import android.webkit.WebSettings
import android.webkit.WebView
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import jp.toastkid.about.R
import java.nio.charset.StandardCharsets

class LicensesDialogFragment : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val context = context ?: return super.onCreateDialog(savedInstanceState)
        val webView = WebView(context)
        webView.settings.also {
            it.javaScriptCanOpenWindowsAutomatically = false
            it.javaScriptEnabled = false
            it.blockNetworkLoads = false
            it.databaseEnabled = false
            it.domStorageEnabled = false
            it.mixedContentMode = WebSettings.MIXED_CONTENT_NEVER_ALLOW
        }

        val dialog = AlertDialog.Builder(context)
            .setTitle(R.string.title_licenses)
            .setView(webView)
            .setPositiveButton(R.string.ok) { d, i -> d.dismiss() }
            .create()

        val content = arguments?.getString(KEY_CONTENT)
            ?: return super.onCreateDialog(savedInstanceState)

        webView.loadDataWithBaseURL(
            null,
            content,
            MIMETYPE,
            encoding,
            null
        )
        return dialog
    }

    companion object {

        private const val MIMETYPE = "text/html"

        private val encoding = StandardCharsets.UTF_8.name()

        private const val KEY_CONTENT = "content"

        fun makeWith(content: String) = LicensesDialogFragment()
            .also { it.arguments = bundleOf(KEY_CONTENT to content) }

    }
}
/*
 * Copyright (c) 2024 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.ui.menu.context.common

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import jp.toastkid.lib.ContentViewModel
import jp.toastkid.ui.R
import jp.toastkid.ui.menu.context.MenuActionCallback

class CommonMenuActionCallback(private val context: Context) : MenuActionCallback {

    private val contentViewModel = (context as? ViewModelStoreOwner)?.let {
        ViewModelProvider(context).get(ContentViewModel::class.java)
    }

    override fun invoke(
        menuId: Int,
        onCopyRequested: (() -> Unit)?,
        onPasteRequested: (() -> Unit)?,
        onCutRequested: (() -> Unit)?,
        onSelectAllRequested: (() -> Unit)?
    ): Boolean = when (menuId) {
        R.id.context_copy -> {
            onCopyRequested?.invoke()
            true
        }
        R.id.context_select_all -> {
            onSelectAllRequested?.invoke()
            true
        }
        R.id.preview_search -> {
            val text = extractSelectedTextWithDirtyAccess(onCopyRequested)
            if (text != null && text.isNotBlank()) {
                contentViewModel?.preview(text.toString())
            }
            true
        }
        R.id.web_search -> {
            val text = extractSelectedTextWithDirtyAccess(onCopyRequested)
            if (text != null && text.isNotBlank()) {
                contentViewModel?.search(text.toString())
            }
            true
        }
        else -> false
    }

    private fun extractSelectedTextWithDirtyAccess(onCopyRequested: (() -> Unit)?): CharSequence? {
        val clipboardManager = clipboardManager(context)
        val present = clipboardManager?.primaryClip

        onCopyRequested?.invoke()
        val primary = clipboardManager?.primaryClip

        clipboardManager?.setPrimaryClip(
            if (present?.getItemAt(0)?.text != null) present
            else ClipData.newPlainText("", "")
        )
        return primary?.getItemAt(0)?.text
    }

    /**
     * Get clipboard.
     *
     * @param context
     */
    private fun clipboardManager(context: Context): ClipboardManager? =
        context.applicationContext.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager?

}
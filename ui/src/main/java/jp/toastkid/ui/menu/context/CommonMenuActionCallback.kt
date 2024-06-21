/*
 * Copyright (c) 2024 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.ui.menu.context

import android.content.Context
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import jp.toastkid.lib.ContentViewModel
import jp.toastkid.lib.clip.Clipboard
import jp.toastkid.ui.R

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
            val present = Clipboard.getPrimary(context)
            onCopyRequested?.invoke()
            val primary = Clipboard.getPrimary(context)
            Clipboard.clip(context, present?.toString() ?: "")
            if (primary != null && primary.isNotBlank()) {
                contentViewModel?.preview(primary.toString())
            }
            true
        }
        R.id.web_search -> {
            val present = Clipboard.getPrimary(context)
            onCopyRequested?.invoke()
            val primary = Clipboard.getPrimary(context)
            Clipboard.clip(context, present?.toString() ?: "")
            if (primary != null && primary.isNotBlank()) {
                contentViewModel?.search(primary.toString())
            }
            true
        }
        else -> false
    }

}
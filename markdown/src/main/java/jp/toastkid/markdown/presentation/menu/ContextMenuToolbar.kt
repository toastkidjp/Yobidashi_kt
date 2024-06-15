/*
 * Copyright (c) 2024 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.markdown.presentation.menu

import android.view.ActionMode
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.platform.TextToolbar
import androidx.compose.ui.platform.TextToolbarStatus
import jp.toastkid.lib.ContentViewModel
import jp.toastkid.lib.clip.Clipboard
import jp.toastkid.markdonw.R
import java.util.concurrent.atomic.AtomicReference

class ContextMenuToolbar(
    private val view: View,
    private val contentViewModel: ContentViewModel,
    private val currentSelection: () -> String
) : TextToolbar {

    private val holder = AtomicReference<ActionMode?>(null)

    override val status: TextToolbarStatus
        get() = if (holder.get() == null) TextToolbarStatus.Hidden else TextToolbarStatus.Shown

    override fun hide() {
        val get = holder.get() ?: return
        get.finish()
        holder.set(null)
    }

    override fun showMenu(
        rect: Rect,
        onCopyRequested: (() -> Unit)?,
        onPasteRequested: (() -> Unit)?,
        onCutRequested: (() -> Unit)?,
        onSelectAllRequested: (() -> Unit)?
    ) {
        val current = holder.get()
        if (current != null) {
            return
        }

        val callback = object : ActionMode.Callback {

            override fun onCreateActionMode(actionMode: ActionMode?, menu: Menu?): Boolean {
                val context = view.context
                val menuInflater = MenuInflater(context)

                menuInflater.inflate(R.menu.context_article_content_search, menu)
                //menuInflater.inflate(R.menu.context_article_content_url, menu)
                return true
            }

            override fun onActionItemClicked(actionMode: ActionMode?, menu: MenuItem?): Boolean {
                val handled = onActionItemId(
                    menu?.itemId ?: -1,
                    onCopyRequested,
                    onSelectAllRequested
                )
                if (handled) {
                    actionMode?.finish()
                    hide()
                }
                return handled
            }

            private fun onActionItemId(
                menuId: Int,
                onCopyRequested: (() -> Unit)?,
                onSelectAllRequested: (() -> Unit)?
            ): Boolean {
                return when (menuId) {
                    R.id.copy -> {
                        onCopyRequested?.invoke()
                        true
                    }
                    R.id.select_all -> {
                        onSelectAllRequested?.invoke()
                        true
                    }
                    R.id.preview_search -> {
                        val present = Clipboard.getPrimary(view.context)
                        onCopyRequested?.invoke()
                        val primary = Clipboard.getPrimary(view.context)
                        Clipboard.clip(view.context, present?.toString() ?: "")
                        if (primary.isNullOrBlank()) {
                            return true
                        }
                        contentViewModel.preview(primary.toString())
                        return true
                    }
                    R.id.web_search -> {
                        /*val selection = currentSelection()
                        if (selection.isEmpty()) {
                            return false
                        }*/
                        val present = Clipboard.getPrimary(view.context)
                        onCopyRequested?.invoke()
                        val primary = Clipboard.getPrimary(view.context)
                        Clipboard.clip(view.context, present?.toString() ?: "")
                        if (primary.isNullOrBlank()) {
                            return true
                        }
                        contentViewModel.search(primary.toString())
                        true
                    }
                    else -> false
                }
            }

            override fun onPrepareActionMode(p0: ActionMode?, p1: Menu?) = false

            override fun onDestroyActionMode(p0: ActionMode?) {
                p0?.hide(0)
                holder.set(null)
            }
        }

        val actionMode = view.startActionMode(callback, ActionMode.TYPE_FLOATING)
        holder.set(actionMode)
    }

}

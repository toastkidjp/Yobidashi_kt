/*
 * Copyright (c) 2024 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.editor.view.menu

import android.view.ActionMode
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.platform.TextToolbar
import androidx.compose.ui.platform.TextToolbarStatus
import androidx.compose.ui.text.input.getSelectedText
import androidx.core.text.isDigitsOnly
import jp.toastkid.editor.R
import jp.toastkid.editor.view.EditorTabViewModel
import jp.toastkid.lib.ContentViewModel
import jp.toastkid.lib.Urls
import jp.toastkid.lib.clip.Clipboard
import java.util.concurrent.atomic.AtomicReference

class CustomTextToolbar(
    private val view: View,
    private val viewModel: EditorTabViewModel,
    private val contextViewModel: ContentViewModel
) : TextToolbar {

    private val holder = AtomicReference<ActionMode?>(null)

    private val menuActionInvoker = MenuActionInvoker(viewModel, view.context, contextViewModel)

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

                if (Urls.isValidUrl(Clipboard.getPrimary(context)?.toString())) {
                    menuInflater.inflate(R.menu.context_editor_clipping_url, menu)
                }
                val textFieldValue = viewModel.content()
                val text = textFieldValue.getSelectedText().text
                if (Urls.isValidUrl(text)) {
                    menuInflater.inflate(R.menu.context_editor_url, menu)
                }
                if (text.isDigitsOnly()) {
                    menuInflater.inflate(R.menu.context_editor_digit, menu)
                }
                if (textFieldValue.getSelectedText().isNotEmpty()) {
                    menuInflater.inflate(R.menu.context_editor_selected, menu)
                }
                menuInflater.inflate(R.menu.context_editor, menu)
                menuInflater.inflate(R.menu.context_speech, menu)
                return true
            }

            override fun onActionItemClicked(actionMode: ActionMode?, menu: MenuItem?): Boolean {
                val handled = menuActionInvoker(
                    menu?.itemId ?: -1,
                    onCopyRequested,
                    onPasteRequested,
                    onCutRequested,
                    onSelectAllRequested
                )
                if (handled) {
                    actionMode?.finish()
                    hide()
                }
                return handled
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
/*
 * Copyright (c) 2024 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.ui.menu.context

import android.view.ActionMode
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.platform.TextToolbar
import androidx.compose.ui.platform.TextToolbarStatus
import java.util.concurrent.atomic.AtomicReference

class ContextMenuToolbar(
    private val view: View,
    private val menuInjector: MenuInjector,
    private val menuActionCallback: MenuActionCallback
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
                menuInjector(menu)
                return true
            }

            override fun onActionItemClicked(actionMode: ActionMode?, menu: MenuItem?): Boolean {
                val handled = menuActionCallback(
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

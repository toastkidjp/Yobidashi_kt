/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.editor

import android.view.ActionMode
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.EditText
import androidx.core.text.isDigitsOnly
import androidx.lifecycle.ViewModelProvider
import jp.toastkid.editor.usecase.MenuActionInvokerUseCase
import jp.toastkid.lib.BrowserViewModel
import jp.toastkid.lib.ContentViewModel
import jp.toastkid.lib.Urls
import jp.toastkid.libs.clip.Clipboard
import jp.toastkid.libs.speech.SpeechMaker

/**
 * @author toastkidjp
 */
class EditorContextMenuInitializer {
    
    operator fun invoke(
        editText: EditText?,
        speechMaker: SpeechMaker?,
        viewModelProvider: ViewModelProvider
    ) {
        if (editText == null) {
            return
        }

        val context = editText.context

        val browserViewModel = viewModelProvider.get(BrowserViewModel::class.java)
        val contentViewModel = viewModelProvider.get(ContentViewModel::class.java)

        val menuActionInvokerUseCase =
            MenuActionInvokerUseCase(editText, speechMaker, browserViewModel, contentViewModel)

        editText.customInsertionActionModeCallback = object : ActionMode.Callback {

            override fun onCreateActionMode(actionMode: ActionMode?, menu: Menu?): Boolean {
                val menuInflater = MenuInflater(context)

                val text = Clipboard.getPrimary(context)?.toString()
                if (Urls.isValidUrl(text)) {
                    menuInflater.inflate(R.menu.context_editor_clipping_url, menu)
                }

                menuInflater.inflate(R.menu.context_editor, menu)
                menuInflater.inflate(R.menu.context_speech, menu)
                return true
            }

            override fun onActionItemClicked(actionMode: ActionMode?, menu: MenuItem?): Boolean {
                val handled = menuActionInvokerUseCase(
                    menu?.itemId ?: -1,
                    extractSelectedText(editText)
                )
                if (handled) {
                    actionMode?.finish()
                }
                return handled
            }

            override fun onPrepareActionMode(p0: ActionMode?, p1: Menu?) = true

            override fun onDestroyActionMode(p0: ActionMode?) = Unit

        }

        editText.customSelectionActionModeCallback = object : ActionMode.Callback {

            override fun onCreateActionMode(actionMode: ActionMode?, menu: Menu?): Boolean {
                val text = extractSelectedText(editText)
                val menuInflater = MenuInflater(context)
                if (Urls.isValidUrl(text)) {
                    menuInflater.inflate(R.menu.context_editor_url, menu)
                }
                if (text.isDigitsOnly()) {
                    menuInflater.inflate(R.menu.context_editor_digit, menu)
                }
                menuInflater.inflate(R.menu.context_editor_selected, menu)
                menuInflater.inflate(R.menu.context_speech, menu)
                return true
            }

            override fun onActionItemClicked(actionMode: ActionMode?, menuItem: MenuItem?): Boolean {
                val handled = menuActionInvokerUseCase(
                    menuItem?.itemId ?: -1,
                    extractSelectedText(editText)
                )
                if (handled) {
                    actionMode?.finish()
                }
                return handled
            }

            override fun onPrepareActionMode(p0: ActionMode?, p1: Menu?) = true

            override fun onDestroyActionMode(p0: ActionMode?) = Unit

        }
    }

    private fun extractSelectedText(editText: EditText): String {
        return editText.text
            .subSequence(editText.selectionStart, editText.selectionEnd)
            .toString()
    }

}
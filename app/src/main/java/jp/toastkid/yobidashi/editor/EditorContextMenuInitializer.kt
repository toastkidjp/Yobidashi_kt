/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.editor

import android.view.ActionMode
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.EditText
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import jp.toastkid.lib.BrowserViewModel
import jp.toastkid.lib.ContentViewModel
import jp.toastkid.lib.Urls
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.editor.usecase.MenuActionInvokerUseCase
import jp.toastkid.yobidashi.libs.clip.Clipboard
import jp.toastkid.yobidashi.libs.speech.SpeechMaker

/**
 * @author toastkidjp
 */
class EditorContextMenuInitializer {
    
    operator fun invoke(
        editText: EditText?,
        speechMaker: SpeechMaker?
    ) {
        if (editText == null) {
            return
        }

        val context = editText.context

        val viewModelProvider = (context as? FragmentActivity)?.let { fragmentActivity ->
            ViewModelProvider(fragmentActivity)
        }

        val browserViewModel = viewModelProvider?.get(BrowserViewModel::class.java)
        val contentViewModel = viewModelProvider?.get(ContentViewModel::class.java)

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
                val handled = invokeMenuAction(
                    menu?.itemId ?: -1,
                    editText, speechMaker,
                    browserViewModel,
                    contentViewModel
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
                menuInflater.inflate(R.menu.context_editor_selected, menu)
                menuInflater.inflate(R.menu.context_speech, menu)
                return true
            }

            override fun onActionItemClicked(actionMode: ActionMode?, menuItem: MenuItem?): Boolean {
                val handled = invokeMenuAction(
                    menuItem?.itemId ?: -1,
                    editText,
                    speechMaker,
                    browserViewModel,
                    contentViewModel
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

    private fun invokeMenuAction(
        itemId: Int,
        editText: EditText,
        speechMaker: SpeechMaker?,
        browserViewModel: BrowserViewModel?,
        contentViewModel: ContentViewModel?
    ): Boolean {
        val text = extractSelectedText(editText)

        return MenuActionInvokerUseCase(editText, speechMaker, browserViewModel, contentViewModel).invoke(itemId, text)
    }

    private fun extractSelectedText(editText: EditText): String {
        return editText.text
            .subSequence(editText.selectionStart, editText.selectionEnd)
            .toString()
    }

}
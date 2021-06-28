/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.editor

import android.content.Context
import android.net.Uri
import android.os.Build
import android.view.ActionMode
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.EditText
import androidx.core.net.toUri
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import jp.toastkid.lib.BrowserViewModel
import jp.toastkid.lib.ContentViewModel
import jp.toastkid.lib.Urls
import jp.toastkid.lib.preference.PreferenceApplier
import jp.toastkid.search.SearchCategory
import jp.toastkid.search.UrlFactory
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.editor.usecase.PasteAsQuotationUseCase
import jp.toastkid.yobidashi.libs.Inputs
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

        val browserViewModel = (context as? FragmentActivity)?.let { fragmentActivity ->
            ViewModelProvider(fragmentActivity).get(BrowserViewModel::class.java)
        }

        val listHeadAdder = ListHeadAdder()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            editText.customInsertionActionModeCallback = object : ActionMode.Callback {

                override fun onCreateActionMode(actionMode: ActionMode?, menu: Menu?): Boolean {
                    val menuInflater = MenuInflater(context)
                    menuInflater.inflate(R.menu.context_editor, menu)
                    menuInflater.inflate(R.menu.context_speech, menu)
                    return true
                }

                override fun onActionItemClicked(actionMode: ActionMode?, menu: MenuItem?): Boolean {
                    val handled = invokeMenuAction(
                        menu?.itemId ?: -1,
                        editText, speechMaker,
                        browserViewModel,
                        listHeadAdder
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
                    listHeadAdder
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
        listHeadAdder: ListHeadAdder
    ): Boolean {
        val text = extractSelectedText(editText)
        val context = editText.context

        when (itemId) {
            R.id.context_edit_insert_as_plain -> {
                val primary = Clipboard.getPrimary(context)
                if (primary.isNullOrEmpty()) {
                    return true
                }

                editText.text.insert(editText.selectionStart, primary)
                return true
            }
            R.id.context_edit_paste_as_quotation -> {
                pasteAsQuotation(context, editText)
                return true
            }
            R.id.context_edit_horizontal_rule -> {
                editText.text.insert(
                    editText.selectionStart,
                    "----${System.lineSeparator()}"
                )
                return true
            }
            R.id.context_edit_duplicate_current_line -> {
                CurrentLineDuplicatorUseCase().invoke(editText)
                return true
            }
            R.id.context_edit_speech -> {
                val speechText = if (text.isBlank()) editText.text.toString() else text
                speechMaker?.invoke(speechText)
                return true
            }
            R.id.context_edit_add_order -> {
                OrderedListHeadAdder().invoke(editText)
                return true
            }
            R.id.context_edit_unordered_list -> {
                listHeadAdder(editText, "-")
                return true
            }
            R.id.context_edit_task_list -> {
                listHeadAdder(editText, "- [ ]")
                return true
            }
            R.id.context_edit_convert_to_table -> {
                TableConverter().invoke(editText)
                return true
            }
            R.id.context_edit_add_quote -> {
                listHeadAdder(editText, ">")
                return true
            }
            R.id.context_edit_double_quote -> {
                StringSurroundingUseCase()(editText, '"')
                return true
            }
            R.id.context_edit_url_open_new -> {
                browserViewModel?.open(text.toUri())
                return true
            }
            R.id.context_edit_url_open_background -> {
                browserViewModel?.openBackground(text.toUri())
                return true
            }
            R.id.context_edit_url_preview -> {
                browserViewModel?.preview(text.toUri())
                Inputs.hideKeyboard(editText)
                return true
            }
            R.id.context_edit_preview_search -> {
                browserViewModel?.preview(makeSearchResultUrl(context, text))
                return true
            }
            R.id.context_edit_web_search -> {
                browserViewModel?.open(makeSearchResultUrl(context, text))
                return true
            }
            else -> Unit
        }
        return false
    }

    private fun makeSearchResultUrl(context: Context, text: String): Uri = UrlFactory().invoke(
        PreferenceApplier(context).getDefaultSearchEngine()
            ?: SearchCategory.getDefaultCategoryName(),
        text
    )

    private fun extractSelectedText(editText: EditText): String {
        return editText.text
            .subSequence(editText.selectionStart, editText.selectionEnd)
            .toString()
    }

    private fun pasteAsQuotation(context: Context, editText: EditText) {
        val fragmentActivity = (context as? FragmentActivity) ?: return

        PasteAsQuotationUseCase(
            editText,
            ViewModelProvider(fragmentActivity).get(ContentViewModel::class.java)
        ).invoke()
    }

}
/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.editor

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
import jp.toastkid.lib.Urls
import jp.toastkid.lib.preference.PreferenceApplier
import jp.toastkid.search.SearchCategory
import jp.toastkid.search.UrlFactory
import jp.toastkid.yobidashi.R
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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            editText.customInsertionActionModeCallback = object : ActionMode.Callback {

                override fun onCreateActionMode(actionMode: ActionMode?, menu: Menu?): Boolean {
                    val menuInflater = MenuInflater(context)
                    menuInflater.inflate(R.menu.context_editor, menu)
                    menuInflater.inflate(R.menu.context_speech, menu)
                    return true
                }

                override fun onActionItemClicked(actionMode: ActionMode?, menu: MenuItem?): Boolean {
                    when (menu?.itemId) {
                        R.id.context_edit_insert_as_plain -> {
                            val primary = Clipboard.getPrimary(context)
                            if (primary.isNullOrEmpty()) {
                                return true
                            }

                            editText.text.insert(editText.selectionStart, primary)
                            actionMode?.finish()
                            return true
                        }
                        R.id.context_edit_paste_as_quotation -> {
                            PasteAsConfirmationDialogFragment.show(context)
                            actionMode?.finish()
                            return true
                        }
                        R.id.context_edit_horizontal_rule -> {
                            editText.text.insert(
                                    editText.selectionStart,
                                    "----${System.getProperty("line.separator")}"
                            )
                            actionMode?.finish()
                            return true
                        }
                        R.id.context_edit_speech -> {
                            speechMaker?.invoke(editText.text.toString())
                            actionMode?.finish()
                            return true
                        }
                        else -> Unit
                    }
                    return false
                }

                override fun onPrepareActionMode(p0: ActionMode?, p1: Menu?) = true

                override fun onDestroyActionMode(p0: ActionMode?) = Unit

            }
        }

        val browserViewModel = (context as? FragmentActivity)?.let { fragmentActivity ->
            ViewModelProvider(fragmentActivity).get(BrowserViewModel::class.java)
        }

        editText.customSelectionActionModeCallback = object : ActionMode.Callback {

            private val listHeadAdder = ListHeadAdder()

            override fun onCreateActionMode(actionMode: ActionMode?, menu: Menu?): Boolean {
                val text = extractSelectedText()
                if (Urls.isValidUrl(text)) {
                    MenuInflater(context).inflate(R.menu.context_editor_url, menu)
                }
                MenuInflater(context).inflate(R.menu.context_editor_selected, menu)
                MenuInflater(context).inflate(R.menu.context_speech, menu)
                return true
            }

            override fun onActionItemClicked(actionMode: ActionMode?, menuItem: MenuItem?): Boolean {
                val text = extractSelectedText()
                when (menuItem?.itemId) {
                    R.id.context_edit_add_order -> {
                        listHeadAdder(editText, "1.")
                        return true
                    }
                    R.id.context_edit_add_minus -> {
                        listHeadAdder(editText, "-")
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
                        actionMode?.finish()
                        return true
                    }
                    R.id.context_edit_url_open_background -> {
                        browserViewModel?.openBackground(text.toUri())
                        actionMode?.finish()
                        return true
                    }
                    R.id.context_edit_url_preview -> {
                        browserViewModel?.preview(text.toUri())
                        Inputs.hideKeyboard(editText)
                        actionMode?.finish()
                        return true
                    }
                    R.id.context_edit_preview_search -> {
                        browserViewModel?.preview(makeSearchResultUrl(text))
                        actionMode?.finish()
                        return true
                    }
                    R.id.context_edit_web_search -> {
                        browserViewModel?.open(makeSearchResultUrl(text))
                        actionMode?.finish()
                        return true
                    }
                    R.id.context_edit_speech -> {
                        speechMaker?.invoke(text)
                        actionMode?.finish()
                        return true
                    }
                    else -> Unit
                }
                return false
            }

            private fun makeSearchResultUrl(text: String): Uri = UrlFactory().invoke(
                    PreferenceApplier(context).getDefaultSearchEngine()
                            ?: SearchCategory.getDefaultCategoryName(),
                    text
            )

            private fun extractSelectedText(): String {
                return editText.text
                        .subSequence(editText.selectionStart, editText.selectionEnd)
                        .toString()
            }

            override fun onPrepareActionMode(p0: ActionMode?, p1: Menu?) = true

            override fun onDestroyActionMode(p0: ActionMode?) = Unit

        }
    }

}
/*
 * Copyright (c) 2024 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.editor.view.menu

import android.content.Context
import androidx.core.net.toUri
import jp.toastkid.editor.R
import jp.toastkid.editor.view.EditorTabViewModel
import jp.toastkid.editor.view.menu.text.CommaInserter
import jp.toastkid.editor.view.menu.text.LinkFormInsertion
import jp.toastkid.editor.view.menu.text.ListHeadAdder
import jp.toastkid.editor.view.menu.text.NumberedListHeadAdder
import jp.toastkid.editor.view.menu.text.PasteAsQuotation
import jp.toastkid.editor.view.menu.text.TableFormConverter
import jp.toastkid.editor.view.menu.text.TextCounter
import jp.toastkid.lib.ContentViewModel
import jp.toastkid.lib.clip.Clipboard
import jp.toastkid.lib.translate.TranslationUrlGenerator
import jp.toastkid.libs.speech.SpeechMaker
import jp.toastkid.ui.menu.context.MenuActionCallback

class MenuActionInvoker(
    private val viewModel: EditorTabViewModel,
    private val context: Context,
    private val contentViewModel: ContentViewModel
) : MenuActionCallback {

    override operator fun invoke(
        menuId: Int,
        onCopyRequested: (() -> Unit)?,
        onPasteRequested: (() -> Unit)?,
        onCutRequested: (() -> Unit)?,
        onSelectAllRequested: (() -> Unit)?
    ): Boolean {
        when (menuId) {
            R.id.context_edit_copy -> {
                onCopyRequested?.invoke()
                return true
            }
            R.id.context_edit_select_all -> {
                onSelectAllRequested?.invoke()
                return true
            }
            R.id.context_edit_select_to_end -> {
                viewModel.selectToEnd()
                return true
            }
            R.id.context_edit_cut -> {
                onCutRequested?.invoke()
                return true
            }
            R.id.context_edit_insert_as_plain -> {
                val primary = Clipboard.getPrimary(context)
                if (primary.isNullOrEmpty()) {
                    return true
                }

                viewModel.insertText(primary)
                return true
            }
            R.id.context_edit_paste_as_quotation -> {
                PasteAsQuotation(context, viewModel, contentViewModel).invoke()
                return true
            }
            R.id.context_edit_paste_url_with_title -> {
                LinkFormInsertion(context, viewModel, contentViewModel).invoke()
                return true
            }
            R.id.context_edit_horizontal_rule -> {
                viewModel.insertText("----${System.lineSeparator()}")
                return true
            }
            R.id.context_edit_duplicate_current_line -> {
                viewModel.duplicateCurrentLine()
                return true
            }
            R.id.context_edit_select_current_line -> {
                viewModel.selectCurrentLine()
                return true
            }
            R.id.context_edit_delete_line -> {
                viewModel.deleteCurrentLine()
                return true
            }
            R.id.context_edit_speech -> {
                val speechText = viewModel.selectedText()
                    .ifBlank { viewModel.content().text.toString() }
                SpeechMaker(context).invoke(speechText)
                return true
            }
            R.id.context_edit_add_order -> {
                val newText = NumberedListHeadAdder().invoke(viewModel.selectedText()) ?: return true
                viewModel.replaceText(newText)
                return true
            }
            R.id.context_edit_unordered_list -> {
                val newText = ListHeadAdder().invoke(viewModel.selectedText(), "-") ?: return true
                viewModel.replaceText(newText)
                return true
            }
            R.id.context_edit_task_list -> {
                val newText = ListHeadAdder().invoke(viewModel.selectedText(), "- [ ]") ?: return true
                viewModel.replaceText(newText)
                return true
            }
            R.id.context_edit_convert_to_table -> {
                val newText = TableFormConverter().invoke(viewModel.selectedText())
                viewModel.replaceText(newText)
                return true
            }
            R.id.context_edit_add_quote -> {
                val newText = ListHeadAdder().invoke(viewModel.selectedText(), ">") ?: return true
                viewModel.replaceText(newText)
                return true
            }
            R.id.context_edit_code_block -> {
                val lineSeparator = System.lineSeparator()
                viewModel.replaceText("```${lineSeparator}${viewModel.selectedText()}${lineSeparator}```")
                return true
            }
            R.id.context_edit_double_quote -> {
                viewModel.replaceText("\"${viewModel.selectedText()}\"")
                return true
            }
            R.id.context_edit_bold -> {
                viewModel.replaceText("**${viewModel.selectedText()}**")
                return true
            }
            R.id.context_edit_italic -> {
                viewModel.replaceText("***${viewModel.selectedText()}***")
                return true
            }
            R.id.context_edit_strikethrough -> {
                viewModel.replaceText("~~${viewModel.selectedText()}~~")
                return true
            }
            R.id.context_edit_url_open_new -> {
                contentViewModel.open(viewModel.selectedText().toUri())
                return true
            }
            R.id.context_edit_url_open_background -> {
                contentViewModel.openBackground(viewModel.selectedText().toUri())
                return true
            }
            R.id.context_edit_url_preview -> {
                contentViewModel.preview(viewModel.selectedText())
                return true
            }
            R.id.context_edit_preview_search -> {
                contentViewModel.preview(viewModel.selectedText())
                return true
            }
            R.id.context_edit_web_search -> {
                contentViewModel.search(viewModel.selectedText())
                return true
            }
            R.id.context_edit_count -> {
                TextCounter().invoke(context, viewModel, contentViewModel)
                return true
            }
            R.id.context_edit_translate -> {
                contentViewModel.preview(TranslationUrlGenerator().invoke(viewModel.selectedText()))
                return true
            }
            R.id.context_edit_insert_thousand_separator -> {
                val selectedText = viewModel.selectedText()
                val converted = CommaInserter().invoke(selectedText)
                if (converted.isNullOrBlank()) {
                    return false
                }
                viewModel.replaceText(converted)
                return true
            }
            R.id.context_edit_show_app_bar -> {
                contentViewModel.showAppBar()
                return true
            }
            else -> Unit
        }
        return false
    }

}
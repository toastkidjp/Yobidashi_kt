/*
 * Copyright (c) 2024 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.editor.view.menu

import android.content.Context
import androidx.compose.ui.text.input.getSelectedText
import androidx.core.net.toUri
import jp.toastkid.editor.R
import jp.toastkid.editor.view.EditorTabViewModel
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

class MenuActionInvoker(
    private val viewModel: EditorTabViewModel,
    private val context: Context,
    private val contentViewModel: ContentViewModel
) {

    operator fun invoke(
        itemId: Int,
        onCopyRequested: (() -> Unit)?,
        onPasteRequested: (() -> Unit)?,
        onCutRequested: (() -> Unit)?,
        onSelectAllRequested: (() -> Unit)?
    ): Boolean {
        when (itemId) {
            R.id.context_edit_copy -> {
                onCopyRequested?.invoke()
                return true
            }
            R.id.context_edit_select_all -> {
                onSelectAllRequested?.invoke()
                return true
            }
            R.id.context_edit_paste -> {
                onPasteRequested?.invoke()
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
                /*TODO CurrentLineDuplicatorUseCase().invoke(editText)
                viewModel.insertText(content, "----${System.lineSeparator()}")*/
                return true
            }
            R.id.context_edit_select_current_line -> {
                //TODO CurrentLineSelectionUseCase().invoke(editText)
                return true
            }
            R.id.context_edit_speech -> {
                val speechText = viewModel.content().getSelectedText().text
                    .ifBlank { viewModel.content().text.toString() }
                SpeechMaker(context).invoke(speechText)
                return true
            }
            R.id.context_edit_add_order -> {
                val newText = NumberedListHeadAdder().invoke(viewModel.content().getSelectedText().text) ?: return true
                viewModel.replaceText(newText)
                return true
            }
            R.id.context_edit_unordered_list -> {
                val newText = ListHeadAdder().invoke(viewModel.content().getSelectedText().text, "-") ?: return true
                viewModel.replaceText(newText)
                return true
            }
            R.id.context_edit_task_list -> {
                val newText = ListHeadAdder().invoke(viewModel.content().getSelectedText().text, "- [ ]") ?: return true
                viewModel.replaceText(newText)
                return true
            }
            R.id.context_edit_convert_to_table -> {
                val newText = TableFormConverter().invoke(viewModel.content().getSelectedText().text) ?: return true
                viewModel.replaceText(newText)
                return true
            }
            R.id.context_edit_add_quote -> {
                val newText = ListHeadAdder().invoke(viewModel.content().getSelectedText().text, ">") ?: return true
                viewModel.replaceText(newText)
                return true
            }
            /*R.id.context_edit_code_block -> {
                CodeBlockUseCase().invoke(editText, text)
                return true
            }
            R.id.context_edit_double_quote -> {
                StringSurroundingUseCase()(editText, '"')
                return true
            }
            R.id.context_edit_bold -> {
                StringSurroundingUseCase()(editText, "**")
                return true
            }
            R.id.context_edit_italic -> {
                StringSurroundingUseCase()(editText, "*")
                return true
            }
            R.id.context_edit_strikethrough -> {
                StringSurroundingUseCase()(editText, "~~")
                return true
            }
            */
            R.id.context_edit_url_open_new -> {
                contentViewModel.open(viewModel.content().getSelectedText().text.toUri())
                return true
            }
            R.id.context_edit_url_open_background -> {
                contentViewModel.openBackground(viewModel.content().getSelectedText().text.toUri())
                return true
            }
            R.id.context_edit_url_preview -> {
                contentViewModel.preview(viewModel.content().getSelectedText().text)
                return true
            }
            R.id.context_edit_preview_search -> {
                contentViewModel.preview(viewModel.content().getSelectedText().text)
                return true
            }
            R.id.context_edit_web_search -> {
                contentViewModel.search(viewModel.content().getSelectedText().text)
                return true
            }
/*
            R.id.context_edit_delete_line -> {
                CurrentLineDeletionUseCase().invoke(editText)
                return true
            }
            */
            R.id.context_edit_count -> {
                TextCounter().invoke(context, viewModel, contentViewModel)
                return true
            }
            R.id.context_edit_translate -> {
                contentViewModel.preview(TranslationUrlGenerator().invoke(viewModel.content().getSelectedText().text))
                return true
            }/*
            R.id.context_edit_insert_thousand_separator -> {
                ThousandSeparatorInsertionUseCase().invoke(editText, text)
                return true
            }
*/
            else -> Unit
        }
        return false
    }

}
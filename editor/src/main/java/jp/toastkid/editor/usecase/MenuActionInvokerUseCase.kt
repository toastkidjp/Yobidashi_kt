/*
 * Copyright (c) 2021 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.editor.usecase

import android.content.Context
import android.net.Uri
import android.widget.EditText
import androidx.annotation.IdRes
import androidx.core.net.toUri
import jp.toastkid.editor.CurrentLineDuplicatorUseCase
import jp.toastkid.editor.ListHeadAdder
import jp.toastkid.editor.OrderedListHeadAdder
import jp.toastkid.editor.R
import jp.toastkid.editor.StringSurroundingUseCase
import jp.toastkid.editor.TableConverter
import jp.toastkid.lib.ContentViewModel
import jp.toastkid.lib.clip.Clipboard
import jp.toastkid.lib.input.Inputs
import jp.toastkid.lib.preference.PreferenceApplier
import jp.toastkid.lib.translate.TranslationUrlGenerator
import jp.toastkid.libs.speech.SpeechMaker
import jp.toastkid.search.SearchCategory
import jp.toastkid.search.UrlFactory

/**
 * @author toastkidjp
 */
class MenuActionInvokerUseCase(
    private val editText: EditText,
    private val speechMaker: SpeechMaker?,
    private val contentViewModel: ContentViewModel?,
    private val listHeadAdder: ListHeadAdder = ListHeadAdder()
) {

    operator fun invoke(@IdRes itemId: Int, text: String): Boolean {
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
                contentViewModel ?: return true
                PasteAsQuotationUseCase(editText, contentViewModel).invoke()
                return true
            }
            R.id.context_edit_paste_url_with_title -> {
                contentViewModel ?: return true
                LinkFormInsertionUseCase(
                    editText,
                    contentViewModel
                ).invoke()
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
            R.id.context_edit_select_current_line -> {
                CurrentLineSelectionUseCase().invoke(editText)
                return true
            }
            R.id.context_edit_speech -> {
                val speechText = text.ifBlank { editText.text.toString() }
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
            R.id.context_edit_code_block -> {
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
            R.id.context_edit_url_open_new -> {
                openUri(text.toUri())
                return true
            }
            R.id.context_edit_url_open_background -> {
                contentViewModel?.openBackground(text.toUri())
                return true
            }
            R.id.context_edit_url_preview -> {
                contentViewModel?.preview(text.toUri())
                return true
            }
            R.id.context_edit_preview_search -> {
                contentViewModel?.preview(makeSearchResultUrl(context, text))
                return true
            }
            R.id.context_edit_web_search -> {
                openUri(makeSearchResultUrl(context, text))
                return true
            }
            R.id.context_edit_delete_line -> {
                CurrentLineDeletionUseCase().invoke(editText)
                return true
            }
            R.id.context_edit_count -> {
                TextCountUseCase().invoke(editText, contentViewModel)
                return true
            }
            R.id.context_edit_translate -> {
                contentViewModel?.preview(TranslationUrlGenerator().invoke(text).toUri())
                return true
            }
            R.id.context_edit_insert_thousand_separator -> {
                ThousandSeparatorInsertionUseCase().invoke(editText, text)
                return true
            }
            else -> Unit
        }
        return false
    }

    private fun openUri(uri: Uri) {
        Inputs().hideKeyboard(editText)
        contentViewModel?.open(uri)
    }

    private fun makeSearchResultUrl(context: Context, text: String): Uri = UrlFactory().invoke(
        PreferenceApplier(context).getDefaultSearchEngine()
            ?: SearchCategory.getDefaultCategoryName(),
        text
    )

}
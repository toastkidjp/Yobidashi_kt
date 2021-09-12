/*
 * Copyright (c) 2021 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.editor.usecase

import android.content.Context
import android.net.Uri
import android.widget.EditText
import androidx.annotation.IdRes
import androidx.core.net.toUri
import jp.toastkid.lib.BrowserViewModel
import jp.toastkid.lib.ContentViewModel
import jp.toastkid.lib.preference.PreferenceApplier
import jp.toastkid.search.SearchCategory
import jp.toastkid.search.UrlFactory
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.editor.CurrentLineDuplicatorUseCase
import jp.toastkid.yobidashi.editor.ListHeadAdder
import jp.toastkid.yobidashi.editor.OrderedListHeadAdder
import jp.toastkid.yobidashi.editor.StringSurroundingUseCase
import jp.toastkid.yobidashi.editor.TableConverter
import jp.toastkid.yobidashi.libs.Inputs
import jp.toastkid.yobidashi.libs.clip.Clipboard
import jp.toastkid.yobidashi.libs.speech.SpeechMaker

/**
 * @author toastkidjp
 */
class MenuActionInvokerUseCase(
    private val editText: EditText,
    private val speechMaker: SpeechMaker?,
    private val browserViewModel: BrowserViewModel?,
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
            R.id.context_edit_strikethrough -> {
                StringSurroundingUseCase()(editText, "~~")
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
            R.id.context_edit_delete_line -> {
                CurrentLineDeletionUseCase().invoke(editText)
                return true
            }
            R.id.context_edit_count -> {
                TextCountUseCase().invoke(editText, contentViewModel)
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

}
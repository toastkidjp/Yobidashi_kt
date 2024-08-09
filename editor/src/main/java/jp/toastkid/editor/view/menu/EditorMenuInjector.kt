/*
 * Copyright (c) 2024 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.editor.view.menu

import android.content.Context
import android.view.Menu
import android.view.MenuInflater
import androidx.core.text.isDigitsOnly
import jp.toastkid.editor.R
import jp.toastkid.lib.Urls
import jp.toastkid.lib.clip.Clipboard
import jp.toastkid.ui.menu.context.MenuInjector

class EditorMenuInjector(
    private val context: Context,
    private val selectedTextProvider: () -> String
) : MenuInjector {

    override fun invoke(menu: Menu?) {
        val menuInflater = MenuInflater(context)

        if (Urls.isValidUrl(Clipboard.getPrimary(context)?.toString())) {
            menuInflater.inflate(R.menu.context_editor_clipping_url, menu)
        }
        val selectedText = selectedTextProvider()
        if (Urls.isValidUrl(selectedText)) {
            menuInflater.inflate(R.menu.context_editor_url, menu)
        }
        if (selectedText.isNotBlank() && selectedText.isDigitsOnly()) {
            menuInflater.inflate(R.menu.context_editor_digit, menu)
        }
        if (selectedText.isNotEmpty()) {
            menuInflater.inflate(R.menu.context_editor_selected, menu)
        }
        menuInflater.inflate(R.menu.context_editor, menu)
        menuInflater.inflate(R.menu.context_speech, menu)
    }

}
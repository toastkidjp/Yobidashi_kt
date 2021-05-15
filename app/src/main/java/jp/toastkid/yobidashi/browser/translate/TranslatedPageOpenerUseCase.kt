/*
 * Copyright (c) 2021 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.browser.translate

import android.net.Uri
import androidx.core.net.toUri
import jp.toastkid.lib.BrowserViewModel

/**
 * @author toastkidjp
 */
class TranslatedPageOpenerUseCase(private val browserViewModel: BrowserViewModel) {

    operator fun invoke(currentUrl: String?) {
        val uri = "https://translate.googleusercontent.com/translate_c" +
                "?depth=1&nv=1&pto=aue&rurl=translate.google.com&sl=auto&sp=nmt4&tl=en&u=" +
                Uri.encode(currentUrl)
        browserViewModel.open(uri.toUri())
    }

}
/*
 * Copyright (c) 2022 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.lib.translate

import android.net.Uri
import java.util.Locale

class TranslationUrlGenerator {

    operator fun invoke(text: String): String {
        val containsMultiByteCharacter = containsMultiByteCharacter(text)
        val sl = if (containsMultiByteCharacter) Locale.JAPANESE.language else Locale.ENGLISH.language
        val tl = if (containsMultiByteCharacter) Locale.ENGLISH.language else Locale.JAPANESE.language

        val encodedText = Uri.encode(text)

        return "https://translate.google.com/?sl=$sl&tl=$tl&text=$encodedText&op=translate"
    }

    private fun containsMultiByteCharacter(str: String) =
        str.toByteArray().size != str.length

}
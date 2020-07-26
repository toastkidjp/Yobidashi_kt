/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.article_viewer.tokenizer

/**
 * @author toastkidjp
 */
class NgramTokenizer {

    operator fun invoke(text: String?, n: Int): String? {
        if (text.isNullOrBlank()) {
            return text
        }

        val stringBuilder = StringBuilder()
        for (i in 0..(text.length - n)) {
            val token = text.substring(i, (i + n))
            stringBuilder.append(token).append(' ')
        }
        return stringBuilder.toString().trim()
    }

}
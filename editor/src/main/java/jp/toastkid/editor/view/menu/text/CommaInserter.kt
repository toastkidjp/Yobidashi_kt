/*
 * Copyright (c) 2024 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.editor.view.menu.text

class CommaInserter {

    operator fun invoke(text: String?): String? {
        if (text == null) {
            return null
        }

        val toCharArray = text.toCharArray()
        return with(StringBuilder()) {
            (toCharArray.indices).forEach { index ->
                if (toCharArray.size > NUMBER_OF_DIGITS && isNotEmpty() && index % NUMBER_OF_DIGITS == (toCharArray.size % NUMBER_OF_DIGITS)) {
                    append(",")
                }
                append(toCharArray[index])
            }
            toString()
        }
    }

}

private const val NUMBER_OF_DIGITS = 3
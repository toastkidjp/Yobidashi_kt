/*
 * Copyright (c) 2024 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.editor.view.menu.text

class ListHeadAdder {

    /**
     * @param text Nullable [CharSequence]
     * @param head
     */
    operator fun invoke(text: String?, head: String): String? {
        if (text.isNullOrEmpty()) {
            return text
        }

        val putEndLineBreak = if (text.endsWith("\n")) "\n" else ""

        return head + " " + text.trimEnd().replace("\n", "\n$head ") + putEndLineBreak
    }

}
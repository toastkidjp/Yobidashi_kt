/*
 * Copyright (c) 2024 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.editor.view.menu.text

class TableFormConverter {

    operator fun invoke(text: String): String {
        val putEndLineBreak = if (text.endsWith("\n")) "\n" else ""

        return "| ${text.trim().replace(" ", " | ").replace("\n", "\n| ")}$putEndLineBreak"
    }

}
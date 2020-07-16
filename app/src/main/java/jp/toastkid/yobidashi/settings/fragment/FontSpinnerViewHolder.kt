/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.settings.fragment

import android.view.View
import android.widget.TextView
import jp.toastkid.yobidashi.editor.EditorFontSize

/**
 * @author toastkidjp
 */
class FontSpinnerViewHolder(private val itemView: View) {

    fun bind(item: EditorFontSize) {
        val textView = itemView.findViewById<TextView>(android.R.id.text1)
        textView.text = item.size.toString()
    }

}
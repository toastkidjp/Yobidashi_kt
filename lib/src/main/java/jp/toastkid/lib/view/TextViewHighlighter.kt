/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.lib.view

import android.text.Spannable
import android.text.SpannableString
import android.text.style.BackgroundColorSpan
import android.widget.TextView

/**
 * @author toastkidjp
 */
class TextViewHighlighter(private val textView: TextView) {

    operator fun invoke(textToHighlight: String) {
        if (textToHighlight.length <= 1) {
            textView.text = textView.text.toString()
            return
        }
        val tvt = textView.text.toString()
        var ofe = tvt.indexOf(textToHighlight, 0)
        val wordToSpan = SpannableString(textView.text)
        var ofs = 0
        while (ofs < tvt.length && ofe != -1) {
            ofe = tvt.indexOf(textToHighlight, ofs)
            if (ofe == -1)
                break
            else {
                // set color here
                wordToSpan.setSpan(
                        BackgroundColorSpan(-0x100),
                        ofe,
                        ofe + textToHighlight.length,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                textView.setText(wordToSpan, TextView.BufferType.SPANNABLE)
            }
            ofs = ofe + 1
        }
    }
}
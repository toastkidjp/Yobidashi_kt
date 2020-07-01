/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.article_viewer.article.detail

import android.text.Selection
import android.text.Spannable
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.URLSpan
import android.view.MotionEvent
import android.widget.TextView

/**
 * @author toastkidjp
 */
class ContentLinkMovementMethod(private val listener: ((String?) -> Unit)) : LinkMovementMethod() {

    override fun onTouchEvent(widget: TextView, buffer: Spannable, event: MotionEvent): Boolean {
        val action = event.action
        if (action != MotionEvent.ACTION_UP
                && action != MotionEvent.ACTION_DOWN) {
            return super.onTouchEvent(widget, buffer, event)
        }

        val link = extractLinks(event, widget, buffer)
        if (link.isNullOrEmpty()) {
            Selection.removeSelection(buffer)
            return super.onTouchEvent(widget, buffer, event)
        }

        when (action) {
            MotionEvent.ACTION_UP -> listener.invoke((link[0] as? URLSpan)?.url)
            MotionEvent.ACTION_DOWN -> Selection.setSelection(
                    buffer,
                    buffer.getSpanStart(link[0]),
                    buffer.getSpanEnd(link[0])
            )
        }

        return true
    }

    private fun extractLinks(event: MotionEvent, widget: TextView, buffer: Spannable): Array<out ClickableSpan>? {
        var x = event.x.toInt()
        var y = event.y.toInt()
        x -= widget.totalPaddingLeft
        y -= widget.totalPaddingTop
        x += widget.scrollX
        y += widget.scrollY
        val layout = widget.layout
        val line = layout.getLineForVertical(y)
        val off = layout.getOffsetForHorizontal(line, x.toFloat())

        val link = buffer.getSpans(off, off, ClickableSpan::class.java)
        return link
    }

}
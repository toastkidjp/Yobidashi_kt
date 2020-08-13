/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.article_viewer.article.detail

import android.text.util.Linkify
import android.widget.TextView
import java.util.regex.Pattern

/**
 * @author toastkidjp
 */
class LinkGeneratorService {

    operator fun invoke(textView: TextView) {
        embedLinks(
                textView,
                internalLinkPattern,
                Linkify.TransformFilter { matcher, _ ->
                    InternalLinkScheme.makeLink(matcher.group(1))
                }
        )

        embedLinks(
                textView,
                httpPattern,
                Linkify.TransformFilter { matcher, _ -> matcher.group(0) }
        )
    }

    private fun embedLinks(textView: TextView, pattern: Pattern, transformFilter: Linkify.TransformFilter) {
        Linkify.addLinks(textView, pattern, null, null, transformFilter)
    }

    companion object {

        private val internalLinkPattern =
                Pattern.compile("\\[\\[(.+?)\\]\\]", Pattern.DOTALL)

        private val httpPattern =
                Pattern.compile("https?://[a-zA-Z0-9/:%#&~=_!'\\\\\$\\\\?\\\\.\\\\+\\\\*\\\\-]+")

    }
}
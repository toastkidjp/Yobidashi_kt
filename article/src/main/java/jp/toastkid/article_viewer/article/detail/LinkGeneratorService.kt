/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.article_viewer.article.detail

import java.util.regex.Pattern

/**
 * @author toastkidjp
 */
class LinkGeneratorService {

    operator fun invoke(text: String): String {
        return embedLinks(text, internalLinkPattern, 1)
    }

    private fun embedLinks(text: String, pattern: Pattern, group: Int): String {
        var converted = text
        val matcher = pattern.matcher(converted)
        while (matcher.find()) {
            converted = matcher.replaceFirst(InternalLinkScheme().makeLink(matcher.group(group)))
        }
        return converted
    }

    companion object {

        private val internalLinkPattern =
                Pattern.compile("\\[\\[(.+?)\\]\\]", Pattern.DOTALL)

        private val httpPattern =
                Pattern.compile("https?://[a-zA-Z0-9/:%#&~=_!'\\\\\$\\\\?\\\\.\\\\+\\\\*\\\\-]+")

    }
}
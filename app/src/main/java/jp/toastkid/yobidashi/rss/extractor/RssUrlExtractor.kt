/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.rss.extractor

import java.util.regex.Pattern

/**
 * @author toastkidjp
 */
class RssUrlExtractor {

    operator fun invoke(html: String?): List<String>? {
        if (html.isNullOrBlank()) {
            return null
        }

        val splitter = if (html.contains(DEFAULT_LINE_SEPARATOR)) DEFAULT_LINE_SEPARATOR else "\n"
        return html.split(splitter)
                .filter {
                    it.contains("<link") && it.contains("alternate")
                }
                .map {
                    val matcher = REGEX.matcher(it)
                    if (matcher.find()) {
                        return@map matcher.group(1)
                    } else {
                        return@map null
                    }
                }
                .filterNotNull()
    }

    companion object {

        private val DEFAULT_LINE_SEPARATOR = System.getProperty("line.separator")

        private val REGEX = Pattern.compile(
                "href=\"(.+?)\"",
                Pattern.DOTALL
        )
    }
}
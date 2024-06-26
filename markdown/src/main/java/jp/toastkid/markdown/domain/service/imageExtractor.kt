/*
 * Copyright (c) 2024 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.markdown.domain.service

import jp.toastkid.markdown.domain.model.data.ImageLine
import java.util.regex.Matcher
import java.util.regex.Pattern

class ImageExtractor {

    operator fun invoke(line: String?): List<ImageLine> {
        if (line.isNullOrBlank()) {
            return emptyList()
        }

        return extractImageUrls(line)
            .filterNotNull()
            .map {
                ImageLine(it)
            }
    }

    /**
     * Extract image url from text.
     * @param line line
     * @return image url
     */
    private fun extractImageUrls(line: String): List<String?> {
        val imageUrls: MutableList<String?> = ArrayList()
        val matcher: Matcher = IMAGE.matcher(line)
        while (matcher.find()) {
            imageUrls.add(matcher.group(2))
        }
        return imageUrls
    }

    companion object {

        /** In-line image pattern.  */
        private val IMAGE: Pattern = Pattern.compile("\\!\\[(.+?)\\]\\((.+?)\\)")

    }

}
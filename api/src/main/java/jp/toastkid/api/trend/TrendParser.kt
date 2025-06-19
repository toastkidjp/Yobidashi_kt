/*
 * Copyright (c) 2023 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.api.trend

import org.jsoup.Jsoup
import org.jsoup.parser.Parser
import timber.log.Timber

/**
 * @author toastkidjp
 */
class TrendParser {

    private val xmlParser = Parser.xmlParser()

    operator fun invoke(content: String): List<Trend> =
            parseContent(content)
                    ?.select("channel > item")
                    ?.map {
                        Trend(
                                it.getElementsByTag("title").text(),
                                it.getElementsByTag("link").text(),
                                it.getElementsByTag("ht:picture").text()
                        )
                    }
                    ?: emptyList()

    private fun parseContent(content: String) = try {
        Jsoup.parse(content, "", xmlParser)
    } catch (e: Throwable) {
        Timber.e(e)
        null
    }

}
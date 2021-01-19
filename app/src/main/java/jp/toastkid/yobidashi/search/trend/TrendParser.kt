/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.search.trend

import org.jsoup.Jsoup

/**
 * @author toastkidjp
 */
class TrendParser {

    operator fun invoke(content: String): List<String> =
            Jsoup.parse(content)
                    .select("channel > item > title")
                    .map { it.text() }
}
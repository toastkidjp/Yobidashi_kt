/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.rss.extractor

import jp.toastkid.yobidashi.libs.Urls

/**
 * @author toastkidjp
 */
class RssUrlValidator {

    operator fun invoke(url: String?): Boolean {
        if (url.isNullOrBlank() || Urls.isInvalidUrl(url)) {
            return false
        }

        if (url.contains("rss") && url.contains("xml")) {
            return true
        }

        return (url.contains(".xml") || url.contains(".atom") || url.contains(".rdf"))
    }
}
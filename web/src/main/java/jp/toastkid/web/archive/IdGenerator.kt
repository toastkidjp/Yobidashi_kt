/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.web.archive

import androidx.core.net.toUri
import jp.toastkid.lib.Urls

/**
 * Use for storing archive file.
 *
 * @author toastkidjp
 */
class IdGenerator {

    /**
     * Generate file ID from passed URL string.
     *
     * @param url URL string(nullable)
     * @return file ID
     */
    fun from(url: String?): String? {
        if (Urls.isInvalidUrl(url)) {
            return url
        }

        return url?.toUri()?.let {
            "${it.host}${it.path?.replace("/", "-") ?: ""}-${it.query ?: ""}"
        }
    }
}
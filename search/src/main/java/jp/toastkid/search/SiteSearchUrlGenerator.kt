/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.search

import android.net.Uri
import androidx.core.net.toUri
import java.util.Formatter

/**
 * @author toastkidjp
 */
class SiteSearchUrlGenerator {

    operator fun invoke(url: String?, rawQuery: String): String =
            Formatter().format(FORMAT, url?.toUri()?.host, Uri.encode(rawQuery)).toString()

    companion object {

        /**
         * Site search URL format.
         */
        private const val FORMAT = "https://www.google.com/search?as_dt=i&as_sitesearch=%s&as_q=%s"
    }
}
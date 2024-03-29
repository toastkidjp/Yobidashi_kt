/*
 * Copyright (c) 2023 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.web.webview.usecase

import android.net.Uri

class ApproachFallbackUrlExtractor {

    operator fun invoke(uri: Uri, consumer: (String) -> Unit) {
        val fallbackUrl = uri.getQueryParameter("fallbackWebURL") ?: return
        consumer(Uri.decode(fallbackUrl))
    }

    fun isTarget(host: String?) = host == "approach.yahoo.co.jp"

}
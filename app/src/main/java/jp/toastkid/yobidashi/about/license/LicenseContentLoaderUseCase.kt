/*
 * Copyright (c) 2021 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.about.license

import android.content.res.AssetManager
import jp.toastkid.licence.LicensesHtmlLoader
import okio.buffer
import okio.source

/**
 * @author toastkidjp
 */
class LicenseContentLoaderUseCase(private val assetManager: AssetManager) {

    operator fun invoke(): String {
        return LicensesHtmlLoader(assetManager).invoke().source().use { source ->
            source.buffer().readUtf8()
        }
    }

}
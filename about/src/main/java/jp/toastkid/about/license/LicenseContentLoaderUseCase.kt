/*
 * Copyright (c) 2021 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.about.license

import android.content.res.AssetManager
import jp.toastkid.licence.LicensesHtmlLoader

/**
 * @author toastkidjp
 */
class LicenseContentLoaderUseCase(private val assetManager: AssetManager) {

    operator fun invoke(): String {
        return LicensesHtmlLoader(assetManager).invoke().bufferedReader().use { source ->
            source.lineSequence().joinToString(System.lineSeparator())
        }
    }

}
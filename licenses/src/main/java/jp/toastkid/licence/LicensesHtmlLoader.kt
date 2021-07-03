/*
 * Copyright (c) 2021 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.licence

import android.content.res.AssetManager
import java.io.InputStream

/**
 * @author toastkidjp
 */
class LicensesHtmlLoader(private val assets: AssetManager) {

    operator fun invoke(): InputStream {
        return assets.open("licenses.html")
    }

}
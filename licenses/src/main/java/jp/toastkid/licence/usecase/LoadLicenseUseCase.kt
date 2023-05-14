/*
 * Copyright (c) 2023 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.licence.usecase

import android.content.res.AssetManager
import jp.toastkid.licence.model.License
import jp.toastkid.licence.model.LicensesLoader

class LoadLicenseUseCase {

    operator fun invoke(assets: AssetManager): List<License> {
        return LicensesLoader().invoke(
            assets.open("licenses.yml").use { String(it.readBytes()) }.split("\n")
        )
    }

}
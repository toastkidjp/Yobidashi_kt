/*
 * Copyright (c) 2021 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.about.license

import android.widget.FrameLayout
import jp.toastkid.about.view.LicensesDialogFragment

/**
 * @author toastkidjp
 */
internal class LicenseHtmlLoaderUseCase {

    operator fun invoke(container: FrameLayout) {
        val content = LicenseContentLoaderUseCase(container.context.assets).invoke()

        LicensesDialogFragment.makeWith(content)
    }

}
/*
 * Copyright (c) 2024 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.ui.menu.context

import android.content.Context
import android.view.Menu
import android.view.MenuInflater
import jp.toastkid.ui.R

class CommonMenuInjector(private val context: Context) : MenuInjector {

    override fun invoke(menu: Menu?) {
        MenuInflater(context).inflate(R.menu.context_common, menu)
    }

}
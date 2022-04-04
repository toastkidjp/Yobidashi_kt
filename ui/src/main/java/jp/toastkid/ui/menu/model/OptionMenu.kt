/*
 * Copyright (c) 2022 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.ui.menu.model

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.runtime.State

data class OptionMenu(
    @DrawableRes val iconId: Int? = null,
    @StringRes val titleId: Int,
    val action: () -> Unit,
    val checkState: State<Boolean>? = null
)

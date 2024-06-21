/*
 * Copyright (c) 2024 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.ui.menu.context

import android.view.View
import androidx.compose.ui.platform.TextToolbar

class CommonContextMenuToolbarFactory {

    operator fun invoke(view: View): TextToolbar {
        return ContextMenuToolbar(
            view,
            CommonMenuInjector(view.context),
            CommonMenuActionCallback(view.context)
        )
    }

}
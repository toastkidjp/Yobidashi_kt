/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.browser.menu

/**
 * @author toastkidjp
 */
interface MenuContract {

    interface View {
        fun onMenuClick(menu: Menu)

        fun onMenuLongClick(menu: Menu): Boolean

        fun getTabCount(): Int

        var menuPresenter: Presenter
    }

    interface Presenter {
        fun switchMenuVisibility()

        fun close()

        fun onResume(additional: (MenuPos) -> Unit)

        fun isVisible(): Boolean

        fun notifyDataSetChanged()
    }
}
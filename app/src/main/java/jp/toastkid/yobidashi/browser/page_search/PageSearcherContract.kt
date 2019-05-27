/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.browser.page_search

import android.app.Activity

/**
 * @author toastkidjp
 */
interface PageSearcherContract {

    interface View {
        fun find(s: String)

        fun findUp(s: String)

        fun findDown(s: String)

        var pageSearchPresenter: PageSearcherContract.Presenter
    }

    interface Presenter {
        /**
         * Implement for Data Binding.
         */
        fun findUp()

        /**
         * Implement for Data Binding.
         */
        fun findDown()

        /**
         * Implement for Data Binding.
         */
        fun clearInput()

        fun isVisible(): Boolean
        /**
         * Show module with opening software keyboard.
         *
         * @param activity [Activity]
         */
        fun show(activity: Activity)

        /**
         * Hide module.
         */
        fun hide()

        fun dispose()
    }
}
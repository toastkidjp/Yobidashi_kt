/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.browser.webview

/**
 * @author toastkidjp
 */
data class LongTapItemHolder(var title: String = "", var anchor: String = "") {
    fun reset() {
        title = ""
        anchor = ""
    }
}
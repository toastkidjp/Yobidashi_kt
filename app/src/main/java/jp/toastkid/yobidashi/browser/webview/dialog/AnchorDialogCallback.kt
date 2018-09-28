/*
 * Copyright (c) 2018 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.browser.webview.dialog

/**
 * @author toastkidjp
 */
interface AnchorDialogCallback {
    fun openNewTab(url: String)
    fun openBackgroundTab(url: String)
    fun openCurrent(url: String)
    fun preview(url: String)
}
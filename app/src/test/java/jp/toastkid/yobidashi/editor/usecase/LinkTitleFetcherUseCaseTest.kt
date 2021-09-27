/*
 * Copyright (c) 2021 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.yobidashi.editor.usecase

import org.junit.Assert.assertEquals
import org.junit.Ignore

class LinkTitleFetcherUseCaseTest {

    @Ignore("Because this function is used network connection.")
    fun testWithRealNetwork() {
        assertEquals(
            "投資信託のモーニングスター｜投資信託・株式・国内/海外ＥＴＦ（上場投資信託）・為替/指数　マーケット情報サイト",
            LinkTitleFetcherUseCase()
                .invoke("https://www.morningstar.co.jp")
        )
    }

}
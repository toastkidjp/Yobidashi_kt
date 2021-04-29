/*
 * Copyright (c) 2021 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.yobidashi.browser.model

import jp.toastkid.yobidashi.tab.History
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * @author toastkidjp
 */
class LoadInformationTest {

    @Test
    fun testExpired() {
        val loadInformation = LoadInformation("test-id", History.EMPTY, -1)
        assertTrue(loadInformation.expired())
    }

    @Test
    fun testDoesNotExpired() {
        val loadInformation = LoadInformation("test-id", History.EMPTY, System.currentTimeMillis())
        assertFalse(loadInformation.expired())
    }

}
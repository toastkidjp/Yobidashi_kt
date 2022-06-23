/*
 * Copyright (c) 2022 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.lib.model

import org.junit.Assert
import org.junit.Test

class LoadInformationTest {

    @Test
    fun testExpired() {
        val loadInformation = LoadInformation("test-id", "title", "https://www.yahoo.co.jp",  -1)
        Assert.assertTrue(loadInformation.expired())
    }

    @Test
    fun testDoesNotExpired() {
        val loadInformation = LoadInformation("test-id", "title", "https://www.yahoo.co.jp", System.currentTimeMillis())
        Assert.assertFalse(loadInformation.expired())
    }

    @Test
    fun testDefaultCase() {
        val loadInformation = LoadInformation("test-id", "title", "https://www.yahoo.co.jp")
        Assert.assertFalse(loadInformation.expired())
    }

}
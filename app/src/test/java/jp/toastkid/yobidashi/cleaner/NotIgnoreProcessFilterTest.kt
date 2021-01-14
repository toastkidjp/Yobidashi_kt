/*
 * Copyright (c) 2021 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.yobidashi.cleaner

import android.content.pm.ApplicationInfo
import io.mockk.MockKAnnotations
import io.mockk.impl.annotations.MockK
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class NotIgnoreProcessFilterTest {

    private lateinit var filter: NotIgnoreProcessFilter

    @MockK
    private lateinit var applicationInfo: ApplicationInfo

    @Before
    fun setUp() {
        filter = NotIgnoreProcessFilter()

        MockKAnnotations.init(this)
    }

    @Test
    fun testIsNotTargetCase() {
        applicationInfo.flags = 0
        applicationInfo.packageName = "jp.co.android.test"

        assertTrue(filter.invoke(applicationInfo))
    }

    @Test
    fun testIsTargetCase() {
        applicationInfo.flags = 0
        applicationInfo.packageName = "jp.toastkid.yobidashi.d"

        assertFalse(filter.invoke(applicationInfo))
    }

    @After
    fun tearDown() {
    }
}
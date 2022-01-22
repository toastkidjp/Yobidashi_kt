/*
 * Copyright (c) 2021 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.licence

import android.content.res.AssetManager
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.unmockkAll
import io.mockk.verify
import org.junit.After
import org.junit.Before
import org.junit.Test

class LicensesHtmlLoaderTest {

    @InjectMockKs
    private lateinit var licensesHtmlLoader: LicensesHtmlLoader

    @MockK
    private lateinit var assets: AssetManager

    @Before
    fun setUp() {
        MockKAnnotations.init(this)

        every { assets.open(any()) }.returns(mockk())
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun invoke() {
        licensesHtmlLoader.invoke()

        verify(exactly = 1) { assets.open(any()) }
    }
}
/*
 * Copyright (c) 2022 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.article_viewer.zip

import android.content.Intent
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.unmockkAll
import io.mockk.verify
import org.junit.After
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test

class ZipLoadProgressBroadcastIntentFactoryTest {
    
    @InjectMockKs
    private lateinit var zipLoadProgressBroadcastIntentFactory: ZipLoadProgressBroadcastIntentFactory
    
    @Before
    fun setUp() {
        MockKAnnotations.init(this)

        mockkConstructor(Intent::class)
        every { anyConstructed<Intent>().putExtra(any(), any<Int>()) }.returns(mockk())
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun testInvoke() {
        zipLoadProgressBroadcastIntentFactory.invoke(100)
        
        verify { anyConstructed<Intent>().putExtra(any(), any<Int>()) }
    }

    @Test
    fun testMakeProgressBroadcastIntentFilter() {
        val intentFilter =
            ZipLoadProgressBroadcastIntentFactory.makeProgressBroadcastIntentFilter()

        assertNotNull(intentFilter)
    }

}
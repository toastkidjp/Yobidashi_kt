/*
 * Copyright (c) 2021 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.yobidashi.browser.shortcut

import android.content.Context
import android.content.Intent
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import androidx.core.content.ContextCompat
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import jp.toastkid.yobidashi.libs.VectorToBitmap
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * @author toastkidjp
 */
class ShortcutUseCaseTest {

    @InjectMockKs
    private lateinit var shortcutUseCase: ShortcutUseCase

    @MockK
    private lateinit var context: Context

    @MockK
    private lateinit var intent: Intent

    @MockK
    private lateinit var shortcutManager: ShortcutManager

    @MockK
    private lateinit var intentFactory: (String) -> Intent

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        every { intentFactory.invoke(any()) }.returns(intent)
        every { intent.setAction(any()) }.returns(intent)
        every { intent.setClass(any(), any()) }.returns(intent)
        every { intent.setData(any()) }.returns(intent)
        every { intent.putExtra(any(), any<String>()) }.returns(intent)
        every { intent.putExtra(any(), any<Intent>()) }.returns(intent)
        every { context.getApplicationContext() }.returns(context)
        every { context.sendBroadcast(any()) }.returns(Unit)

        mockkConstructor(ShortcutInfo.Builder::class)
        val shortcutInfoBuilder = mockk<ShortcutInfo.Builder>()
        every { anyConstructed<ShortcutInfo.Builder>().setShortLabel(any()) }.returns(shortcutInfoBuilder)
        every { shortcutInfoBuilder.setLongLabel(any()) }.returns(shortcutInfoBuilder)
        every { shortcutInfoBuilder.setIcon(any()) }.returns(shortcutInfoBuilder)
        every { shortcutInfoBuilder.setIntent(any()) }.returns(shortcutInfoBuilder)
        every { shortcutInfoBuilder.build() }.returns(mockk())

        mockkConstructor(VectorToBitmap::class)
        every { anyConstructed<VectorToBitmap>().invoke(any()) }.returns(mockk())

        mockkStatic(ContextCompat::class)
        every { ContextCompat.getSystemService(any(), any<Class<Any>>()) }.returns(shortcutManager)
        every { shortcutManager.requestPinShortcut(any(), any()) }.returns(true)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun invoke() {
        shortcutUseCase.invoke(mockk(), "test", mockk())
    }

}
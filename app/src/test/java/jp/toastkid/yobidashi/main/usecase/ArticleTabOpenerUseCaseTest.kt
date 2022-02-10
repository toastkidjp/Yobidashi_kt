/*
 * Copyright (c) 2022 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.yobidashi.main.usecase

import android.view.View
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkAll
import io.mockk.verify
import jp.toastkid.yobidashi.libs.Toaster
import jp.toastkid.yobidashi.tab.TabAdapter
import org.junit.After
import org.junit.Before
import org.junit.Test

class ArticleTabOpenerUseCaseTest {

    @InjectMockKs
    private lateinit var articleTabOpenerUseCase: ArticleTabOpenerUseCase

    @MockK
    private lateinit var tabs: TabAdapter

    @MockK
    private lateinit var snackbarParent: View

    @MockK
    private lateinit var replaceToCurrentTab: () -> Unit

    @Before
    fun setUp() {
        MockKAnnotations.init(this)

        every { tabs.openNewArticleTab(any(), any()) }.returns(mockk())
        every { replaceToCurrentTab.invoke() }.just(Runs)
        every { tabs.replace(any()) }.just(Runs)
        every { snackbarParent.context.getString(any(), any()) }.returns("test message")
        every { snackbarParent.context.getString(any()) }.returns("test")

        mockkObject(Toaster)
        every { Toaster.withAction(any(), any(), any<String>(), any(), any(), any()) }
            .returns(mockk())
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun testOpenBackgroundCase() {
        articleTabOpenerUseCase.invoke("title", true, mockk())

        verify { tabs.openNewArticleTab(any(), any()) }
        verify(inverse = true) { replaceToCurrentTab.invoke() }
        verify { Toaster.withAction(any(), any(), any<String>(), any(), any(), any()) }
    }

    @Test
    fun testOpenForegroundCase() {
        articleTabOpenerUseCase.invoke("title", false, mockk())

        verify { tabs.openNewArticleTab(any(), any()) }
        verify { replaceToCurrentTab.invoke() }
        verify(inverse = true) { Toaster.withAction(any(), any(), any<String>(), any(), any(), any()) }
    }
}
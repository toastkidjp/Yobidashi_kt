/*
 * Copyright (c) 2022 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.article_viewer.article.list.menu

import android.content.Context
import android.widget.PopupWindow
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.unmockkAll
import io.mockk.verify
import jp.toastkid.article_viewer.article.list.SearchResult
import org.junit.After
import org.junit.Before
import org.junit.Test

class MenuPopupTest {

    private lateinit var menuPopup: MenuPopup

    @MockK
    private lateinit var context: Context

    @MockK
    private lateinit var action: MenuPopupActionUseCase

    private val useAddToBookmark: Boolean = true

    @MockK
    private lateinit var menuPopupView: MenuPopupView

    @RelaxedMockK
    private lateinit var searchResult: SearchResult

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        every { context.resources.getDimensionPixelSize(any()) }.returns(100)
        every { menuPopupView.contentView() }.returns(mockk())
        every { menuPopupView.setPopup(any()) }.just(Runs)
        every { menuPopupView.setVisibility(any()) }.just(Runs)
        every { action.addToBookmark(any()) }.just(Runs)
        every { action.delete(any()) }.just(Runs)
        every { searchResult.id }.returns(1)

        mockkConstructor(PopupWindow::class)

        every { anyConstructed<PopupWindow>().contentView = any() }.just(Runs)
        every { anyConstructed<PopupWindow>().isOutsideTouchable = any() }.just(Runs)
        every { anyConstructed<PopupWindow>().width = any() }.just(Runs)
        every { anyConstructed<PopupWindow>().height = any() }.just(Runs)
        every { anyConstructed<PopupWindow>().showAsDropDown(any()) }.just(Runs)
        every { anyConstructed<PopupWindow>().dismiss() }.just(Runs)

        menuPopup = MenuPopup(context, action, useAddToBookmark, menuPopupView)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun show() {
        menuPopup.show(mockk(), searchResult)

        verify { anyConstructed<PopupWindow>().showAsDropDown(any()) }
    }
}
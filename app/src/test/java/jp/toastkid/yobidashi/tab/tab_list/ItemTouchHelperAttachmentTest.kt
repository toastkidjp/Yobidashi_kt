/*
 * Copyright (c) 2021 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.yobidashi.tab.tab_list

import android.content.res.Resources
import android.view.ViewConfiguration
import androidx.recyclerview.widget.RecyclerView
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import io.mockk.verify
import org.junit.After
import org.junit.Before
import org.junit.Test

class ItemTouchHelperAttachmentTest {

    private lateinit var itemTouchHelperAttachment: ItemTouchHelperAttachment

    @MockK
    private lateinit var recyclerView: RecyclerView

    @MockK
    private lateinit var resources: Resources

    @MockK
    private lateinit var viewConfiguration: ViewConfiguration

    @Before
    fun setUp() {
        MockKAnnotations.init(this)

        itemTouchHelperAttachment = ItemTouchHelperAttachment()

        every { recyclerView.getContext() }.returns(mockk())
        every { recyclerView.getResources() }.returns(resources)
        every { recyclerView.addItemDecoration(any()) }.answers { Unit }
        every { recyclerView.addOnItemTouchListener(any()) }.answers { Unit }
        every { recyclerView.addOnChildAttachStateChangeListener(any()) }.answers { Unit }
        every { resources.getDimension(any()) }.returns(1f)

        mockkStatic(ViewConfiguration::class)
        every { ViewConfiguration.get(any()) }.returns(viewConfiguration)
        every { viewConfiguration.getScaledTouchSlop() }.returns(1)
        every { viewConfiguration.getScaledDoubleTapSlop() }.returns(1)
        every { viewConfiguration.getScaledMinimumFlingVelocity() }.returns(1)
        every { viewConfiguration.getScaledMaximumFlingVelocity() }.returns(1)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun invoke() {
        itemTouchHelperAttachment.invoke(recyclerView)

        verify(atLeast = 1) { recyclerView.getContext() }
        verify(exactly = 1) { recyclerView.getResources() }
        verify(exactly = 1) { recyclerView.addItemDecoration(any()) }
        verify(exactly = 1) { recyclerView.addOnItemTouchListener(any()) }
        verify(exactly = 1) { recyclerView.addOnChildAttachStateChangeListener(any()) }
        verify(atLeast = 1) { resources.getDimension(any()) }
        verify(atLeast = 1) { ViewConfiguration.get(any()) }
        verify(atLeast = 1) { viewConfiguration.getScaledTouchSlop() }
        verify(exactly = 1) { viewConfiguration.getScaledDoubleTapSlop() }
        verify(exactly = 1) { viewConfiguration.getScaledMinimumFlingVelocity() }
        verify(exactly = 1) { viewConfiguration.getScaledMaximumFlingVelocity() }
    }

}
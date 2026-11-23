/*
 * Copyright (c) 2026 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.web.view

import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.unmockkAll
import io.mockk.verify
import jp.toastkid.web.video.seek.SeekScriptFactory
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class WebTabUiViewModelTest {

    private lateinit var subject: WebTabUiViewModel

    @MockK
    private lateinit var seekScriptFactory: SeekScriptFactory

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        every { seekScriptFactory.isTarget(any()) } returns false

        subject = WebTabUiViewModel(seekScriptFactory)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun icon() {
        assertNull(subject.icon())

        subject.newIcon(mockk())

        assertNotNull(subject.icon())

        subject.resetIcon()

        assertNull(subject.icon())
    }

    @Test
    fun title() {
        assertTrue(subject.title().isEmpty())

        subject.nextTitle(null)

        assertTrue(subject.title().isEmpty())

        subject.nextTitle("test")

        assertEquals("test", subject.title())
    }

    @Test
    fun url() {
        assertTrue(subject.url().isEmpty())

        subject.nextUrl(null)

        assertTrue(subject.url().isEmpty())

        subject.nextUrl("test")

        assertEquals("test", subject.url())
    }

    @Test
    fun enableForward() {
        assertFalse(subject.enableForward())

        subject.setForwardButtonIsEnabled(true)

        assertTrue(subject.enableForward())
    }

    @Test
    fun enableBack() {
        assertFalse(subject.enableBack())

        subject.setBackButtonIsEnabled(true)

        assertTrue(subject.enableBack())
    }

    @Test
    fun progress() {
        assertEquals(100, subject.progress())
        assertFalse(subject.shouldShowProgressIndicator())

        subject.updateProgress(10)

        assertEquals(10, subject.progress())
        assertTrue(subject.shouldShowProgressIndicator())
    }

    @Test
    fun openErrorDialog() {
        assertFalse(subject.openErrorDialog())
        assertTrue(subject.error().isEmpty())

        subject.setError("test")

        assertTrue(subject.openErrorDialog())
        assertEquals("test", subject.error())

        subject.clearError()

        assertFalse(subject.openErrorDialog())
        assertTrue(subject.error().isEmpty())
    }

    @Test
    fun getLongTapActionParameters() {
        assertFalse(subject.isOpenLongTapDialog())

        subject.setLongTapParameters("", "", "")

        assertTrue(subject.isOpenLongTapDialog())

        val (first, second, third) = subject.longTapActionParameters.value
        assertTrue(first!!.isEmpty())
        assertTrue(second!!.isEmpty())
        assertTrue(third!!.isEmpty())

        subject.clearLongTapParameters()

        assertFalse(subject.isOpenLongTapDialog())
    }

    @Test
    fun showReader() {
        assertFalse(subject.isOpenReaderMode())

        subject.showReader("test", mockk())

        assertTrue(subject.isOpenReaderMode())
        assertEquals("test", subject.readerModeText())

        subject.closeReaderMode()

        assertFalse(subject.isOpenReaderMode())
    }

    @Test
    fun setRefreshing() {
        assertFalse(subject.isRefreshing())

        subject.setRefreshing()

        assertTrue(subject.isRefreshing())

        subject.stopProgress()

        assertFalse(subject.isRefreshing())
    }

    @Test
    fun isBackwardSeekTarget() {
        subject.isBackwardSeekTarget("test")

        verify { seekScriptFactory.isTarget(any()) }
    }

}
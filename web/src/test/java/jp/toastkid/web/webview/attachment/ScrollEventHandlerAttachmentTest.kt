/*
 * Copyright (c) 2026 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.web.webview.attachment

import io.mockk.MockKAnnotations
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.verify
import org.junit.Before
import org.junit.Test

class ScrollEventHandlerAttachmentTest {

    private lateinit var subject: ScrollEventHandlerAttachment

    @RelaxedMockK
    private lateinit var showAppBar: () -> Unit

    @RelaxedMockK
    private lateinit var hideAppBar: () -> Unit

    @Before
    fun setUp() {
        MockKAnnotations.init(this)

        subject = ScrollEventHandlerAttachment(showAppBar, hideAppBar)
    }

    @Test
    fun onScroll() {
        subject.onScroll("up")
        verify { showAppBar.invoke() }

        subject.onScroll("down")
        verify { hideAppBar.invoke() }
    }

}

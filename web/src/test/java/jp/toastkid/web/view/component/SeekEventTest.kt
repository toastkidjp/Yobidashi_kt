/*
 * Copyright (c) 2026 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.web.view.component

import org.junit.Assert.assertEquals
import org.junit.Test

class SeekEventTest {

    @Test
    fun toOffsetSeconds() {
        assertEquals(4800, BackwardSeekEvent(1, 20).toOffsetSeconds())
    }

}
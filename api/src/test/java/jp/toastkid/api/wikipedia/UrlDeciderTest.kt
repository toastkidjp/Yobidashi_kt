/*
 * Copyright (c) 2023 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.api.wikipedia

import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Locale

/**
 * [UrlDecider]'s test case.
 *
 * @author toastkidjp
 */
class UrlDeciderTest {

    /**
     * Check for generating URL.
     */
    @Test
    fun test() {
        assertTrue(UrlDecider().invoke().startsWith("https://${Locale.getDefault().language}."))
    }
}
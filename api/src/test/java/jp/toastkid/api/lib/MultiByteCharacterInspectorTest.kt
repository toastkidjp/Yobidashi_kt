/*
 * Copyright (c) 2021 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.api.lib

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * [MultiByteCharacterInspector]' test cases.
 *
 * @author toastkidjp
 */
class MultiByteCharacterInspectorTest {

    private val multiByteCharacterInspector = MultiByteCharacterInspector()

    /**
     * Test of [MultiByteCharacterInspector.invoke].
     */
    @Test
    fun test_containsMultiByte() {
        assertTrue(multiByteCharacterInspector("おはよう"))
        assertTrue(multiByteCharacterInspector("それはB"))
        assertTrue(multiByteCharacterInspector("ＩＴ"))
        assertFalse(multiByteCharacterInspector("abc"))
        assertFalse(multiByteCharacterInspector("123"))
        assertFalse(multiByteCharacterInspector("1b3"))
    }
}

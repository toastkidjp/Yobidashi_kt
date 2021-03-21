/*
 * Copyright (c) 2021 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.yobidashi.rss.api

import org.junit.Assert.assertNotNull
import org.junit.Test

class RssConverterFactoryTest {

    @Test
    fun test() {
        assertNotNull(RssConverterFactory.create())
    }

}
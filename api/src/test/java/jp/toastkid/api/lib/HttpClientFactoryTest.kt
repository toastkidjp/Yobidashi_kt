/*
 * Copyright (c) 2021 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.api.lib

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * @author toastkidjp
 */
class HttpClientFactoryTest {

    @Test
    fun test() {
        val client = HttpClientFactory.withTimeout(1)
        assertEquals(1000, client.connectTimeoutMillis)
        assertEquals(1000, client.readTimeoutMillis)
        assertEquals(1000, client.writeTimeoutMillis)
    }

}
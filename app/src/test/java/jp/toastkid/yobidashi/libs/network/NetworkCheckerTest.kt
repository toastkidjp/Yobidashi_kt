/*
 * Copyright (c) 2021 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.yobidashi.libs.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.unmockkAll
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Test

/**
 * @author toastkidjp
 */
class NetworkCheckerTest {

    @MockK
    private lateinit var context: Context

    @MockK
    private lateinit var connectivityManager: ConnectivityManager

    @MockK
    private lateinit var network: Network

    @MockK
    private lateinit var networkCapabilities: NetworkCapabilities

    @Before
    fun setUp() {
        MockKAnnotations.init(this)

        every { context.getSystemService(any()) }.returns(connectivityManager)
        every { connectivityManager.activeNetwork }.returns(network)
        every { connectivityManager.getNetworkCapabilities(any()) }.returns(networkCapabilities)
        every { networkCapabilities.hasTransport(any()) }.returns(true)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun testIsNotAvailable() {
        assertFalse(NetworkChecker.isNotAvailable(context))
    }

}
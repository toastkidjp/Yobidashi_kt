/*
 * Copyright (c) 2023 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.lib.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.unmockkAll
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * @author toastkidjp
 */
class NetworkCheckerTest {

    @InjectMockKs
    private lateinit var networkChecker: NetworkChecker

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
        assertFalse(networkChecker.isNotAvailable(context))
    }

    @Test
    fun testIsUnavailableWiFi() {
        assertFalse(networkChecker.isUnavailableWiFi(context))
    }

    @Test
    fun testCannotGetConnectivityManager() {
        every { context.getSystemService(any()) }.returns(null)

        assertTrue(networkChecker.isUnavailableWiFi(context))
        assertTrue(networkChecker.isNotAvailable(context))
    }

    @Test
    fun testCannotGetActiveNetwork() {
        every { connectivityManager.activeNetwork }.returns(null)

        assertTrue(networkChecker.isUnavailableWiFi(context))
        assertTrue(networkChecker.isNotAvailable(context))
    }

    @Test
    fun testCannotGetNetworkCapabilities() {
        every { connectivityManager.getNetworkCapabilities(any()) }.returns(null)

        assertTrue(networkChecker.isUnavailableWiFi(context))
        assertTrue(networkChecker.isNotAvailable(context))
    }

}
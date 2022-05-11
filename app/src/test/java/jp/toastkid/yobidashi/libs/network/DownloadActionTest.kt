/*
 * Copyright (c) 2022 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.yobidashi.libs.network

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import io.mockk.verify
import jp.toastkid.lib.preference.PreferenceApplier
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.io.File

class DownloadActionTest {

    @InjectMockKs
    private lateinit var downloadAction: DownloadAction

    @MockK
    private lateinit var context: Context

    @MockK
    private lateinit var folder: File

    @MockK
    private lateinit var downloadManager: DownloadManager

    @MockK
    private lateinit var uri: Uri

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        every { context.getSharedPreferences(any(), any()) }.returns(mockk())
        every { context.getExternalFilesDir(any()) }.returns(folder)
        every { context.getSystemService(any()) }.returns(downloadManager)
        every { folder.exists() }.returns(false)
        every { folder.mkdirs() }.returns(true)
        every { downloadManager.enqueue(any()) }.returns(1)

        mockkConstructor(PreferenceApplier::class)
        every { anyConstructed<PreferenceApplier>().wifiOnly }.returns(true)

        mockkObject(NetworkChecker)
        every { NetworkChecker.isUnavailableWiFi(any()) }.returns(false)

        mockkStatic(Uri::class)
        every { Uri.parse(any()) }.returns(uri)
        every { uri.lastPathSegment }.returns("test")

        val request = mockk<DownloadManager.Request>()
        mockkConstructor(DownloadManager.Request::class)
        every { anyConstructed<DownloadManager.Request>().setNotificationVisibility(any()) }.returns(request)

        every { request.setDestinationInExternalPublicDir(any(), any()) }.returns(request)
        every { request.setAllowedOverMetered(any()) }.returns(request)
        every { request.setAllowedOverRoaming(any()) }.returns(request)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun testNoopOnUnavailableWiFiCase() {
        every { NetworkChecker.isUnavailableWiFi(any()) }.returns(true)

        downloadAction.invoke("https://www.search.yahoo.co.jp")
    }

    @Test
    fun testEnqueueAndMakingFolderCase() {
        downloadAction.invoke("https://www.search.yahoo.co.jp")

        verify { folder.mkdirs() }
        verify { downloadManager.enqueue(any()) }
    }

    @Test
    fun testEnqueueWithoutMakingFolderCase() {
        every { folder.exists() }.returns(true)
        downloadAction.invoke("https://www.search.yahoo.co.jp")

        verify(inverse = true) { folder.mkdirs() }
        verify { downloadManager.enqueue(any()) }
    }

}
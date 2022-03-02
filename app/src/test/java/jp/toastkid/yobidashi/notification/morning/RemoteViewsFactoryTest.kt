/*
 * Copyright (c) 2022 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.yobidashi.notification.morning

import android.app.PendingIntent
import android.content.Context
import android.net.Uri
import android.widget.RemoteViews
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import io.mockk.verify
import jp.toastkid.yobidashi.BuildConfig
import jp.toastkid.yobidashi.main.launch.MainActivityIntentFactory
import jp.toastkid.yobidashi.wikipedia.random.RandomWikipedia
import jp.toastkid.yobidashi.wikipedia.today.DateArticleUrlFactory
import org.junit.After
import org.junit.Before
import org.junit.Test

class RemoteViewsFactoryTest {

    @InjectMockKs
    private lateinit var removeViewsFactory: RemoteViewsFactory

    @MockK
    private lateinit var dateArticleUrlFactory: DateArticleUrlFactory

    @MockK
    private lateinit var mainActivityIntentFactory: MainActivityIntentFactory

    @MockK
    private lateinit var context: Context

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        every { dateArticleUrlFactory.invoke(any(), any(), any()) }.returns("2022-02-22")
        every { mainActivityIntentFactory.browser(any(), any()) }.returns(mockk())
        every { context.packageName }.returns(BuildConfig.APPLICATION_ID)

        mockkConstructor(RemoteViews::class)
        every { anyConstructed<RemoteViews>().setOnClickPendingIntent(any(), any()) }.just(Runs)

        mockkStatic(PendingIntent::class)
        every { PendingIntent.getActivity(any(), any(), any(), any()) }.returns(mockk())

        mockkStatic(Uri::class)
        every { Uri.parse(any()) }.returns(mockk())

        mockkConstructor(RandomWikipedia::class)
        every { anyConstructed<RandomWikipedia>().fetchWithAction(any()) }.just(Runs)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun testInvoke() {
        removeViewsFactory.invoke(context)

        verify { dateArticleUrlFactory.invoke(any(), any(), any()) }
        verify { mainActivityIntentFactory.browser(any(), any()) }
    }

}
/*
 * Copyright (c) 2021 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.yobidashi.main.initial

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.net.Uri
import androidx.core.content.ContextCompat
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.mockk.spyk
import io.mockk.unmockkAll
import io.mockk.verify
import jp.toastkid.lib.preference.PreferenceApplier
import jp.toastkid.search.SearchCategory
import jp.toastkid.yobidashi.browser.FaviconFolderProviderService
import jp.toastkid.yobidashi.browser.bookmark.BookmarkInitializer
import jp.toastkid.yobidashi.settings.background.DefaultBackgroundImagePreparation
import jp.toastkid.yobidashi.settings.color.DefaultColorInsertion
import org.junit.After
import org.junit.Before
import org.junit.Test

class FirstLaunchInitializerTest {

    private lateinit var firstLaunchInitializer: FirstLaunchInitializer

    @MockK
    private lateinit var context: Context

    private lateinit var preferenceApplier: PreferenceApplier

    @MockK
    private lateinit var defaultColorInsertion: DefaultColorInsertion

    @MockK
    private lateinit var faviconFolderProviderService: FaviconFolderProviderService

    @MockK
    private lateinit var defaultBackgroundImagePreparation: DefaultBackgroundImagePreparation

    @MockK
    private lateinit var sharedPreferences: SharedPreferences

    @MockK
    private lateinit var editor: SharedPreferences.Editor

    @Before
    fun setUp() {
        MockKAnnotations.init(this)

        every { context.getSharedPreferences(any(), any()) }.returns(sharedPreferences)
        every { sharedPreferences.edit() }.returns(editor)
        every { editor.putInt(any(), any()) }.returns(editor)
        every { editor.apply() }.just(Runs)

        preferenceApplier = spyk(PreferenceApplier(context))

        every { preferenceApplier.isFirstLaunch() }.returns(true)
        every { defaultColorInsertion.insert(any()) }.returns(mockk())
        every { faviconFolderProviderService.invoke(any()) }.returns(mockk())
        every { defaultBackgroundImagePreparation.invoke(any(), any()) }.returns(mockk())
        every { preferenceApplier.setDefaultSearchEngine(any()) }.just(Runs)

        mockkStatic(ContextCompat::class)
        every { ContextCompat.getColor(any(), any()) }.returns(Color.CYAN)
        mockkStatic(Uri::class)
        val returnValue = mockk<Uri>()
        every { returnValue.host }.returns("yahoo.co.jp")
        every { Uri.parse(any()) }.returns(returnValue)
        mockkObject(SearchCategory)
        every { SearchCategory.getDefaultCategoryName() }.returns("yahoo")
        mockkConstructor(BookmarkInitializer::class)
        every { anyConstructed<BookmarkInitializer>().invoke() }.returns(mockk())

        firstLaunchInitializer = FirstLaunchInitializer(
            context,
            preferenceApplier,
            defaultColorInsertion,
            faviconFolderProviderService,
            defaultBackgroundImagePreparation
        )
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun testInvoke() {
        firstLaunchInitializer.invoke()

        verify(exactly = 1) { preferenceApplier.isFirstLaunch() }
        verify(exactly = 1) { defaultColorInsertion.insert(any()) }
        verify(exactly = 1) { defaultBackgroundImagePreparation.invoke(any(), any()) }
        verify(exactly = 1) { preferenceApplier.setDefaultSearchEngine(any()) }
    }

    @Test
    fun testIsNotFirstLaunch() {
        every { preferenceApplier.isFirstLaunch() }.returns(false)

        firstLaunchInitializer.invoke()

        verify(exactly = 1) { preferenceApplier.isFirstLaunch() }
        verify(exactly = 0) { defaultColorInsertion.insert(any()) }
        verify(exactly = 0) { defaultBackgroundImagePreparation.invoke(any(), any()) }
        verify(exactly = 0) { preferenceApplier.setDefaultSearchEngine(any()) }
    }

}
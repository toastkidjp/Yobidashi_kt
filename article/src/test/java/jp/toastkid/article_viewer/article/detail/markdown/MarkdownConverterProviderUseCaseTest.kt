/*
 * Copyright (c) 2021 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.article_viewer.article.detail.markdown

import android.content.Context
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import io.mockk.verify
import io.noties.markwon.Markwon
import io.noties.markwon.ext.tables.TablePlugin
import io.noties.markwon.ext.tasklist.TaskListPlugin
import io.noties.markwon.syntax.Prism4jThemeDefault
import io.noties.markwon.syntax.SyntaxHighlightPlugin
import org.junit.After
import org.junit.Before
import org.junit.Test

class MarkdownConverterProviderUseCaseTest {

    private lateinit var markdownConverterProviderUseCase: MarkdownConverterProviderUseCase

    @MockK
    private lateinit var context: Context

    @MockK
    private lateinit var builder: Markwon.Builder

    @Before
    fun setUp() {
        MockKAnnotations.init(this)

        markdownConverterProviderUseCase = MarkdownConverterProviderUseCase()

        mockkStatic(TablePlugin::class)
        every { TablePlugin.create(any<Context>()) }.returns(mockk())

        mockkStatic(TaskListPlugin::class)
        every { TaskListPlugin.create(any<Context>()) }.returns(mockk())

        mockkStatic(SyntaxHighlightPlugin::class)
        every { SyntaxHighlightPlugin.create(any(), any()) }.returns(mockk())

        mockkStatic(Prism4jThemeDefault::class)
        every { Prism4jThemeDefault.create() }.returns(mockk())

        mockkStatic(Markwon::class)
        every { Markwon.builder(any()) }.returns(builder)
        every { builder.usePlugins(any()) }.returns(builder)
        every { builder.build() }.returns(mockk())
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun test() {
        markdownConverterProviderUseCase.invoke(context)

        verify(atLeast = 1) { TablePlugin.create(any<Context>()) }
        verify(atLeast = 1) { TaskListPlugin.create(any<Context>()) }
        verify(atLeast = 1) { SyntaxHighlightPlugin.create(any(), any()) }
        verify(atLeast = 1) { Prism4jThemeDefault.create() }
        verify(atLeast = 1) { Markwon.builder(any()) }
        verify(atLeast = 1) { builder.usePlugins(any()) }
        verify(atLeast = 1) { builder.build() }
    }

}
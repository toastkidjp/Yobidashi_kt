/*
 * Copyright (c) 2022 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.article_viewer.article.detail

import io.mockk.MockKAnnotations
import io.mockk.impl.annotations.InjectMockKs
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class LinkGeneratorTest {

    @InjectMockKs
    private lateinit var linkGenerator: LinkGenerator

    private val markdown = """
        1. [[『御伽草子』]] 4
        1. [[『存在の耐えられない軽さ』感想]] 4
        1. [[『あしながおじさん』感想]] 6
        1. [[『続あしながおじさん』感想]] 5
        1. [[『武士道』感想]] 6
        1. [[『阿Ｑ正伝』感想]] 5
        https://www.yahoo.co.jp
    """.trimIndent()

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
    }

    @After
    fun tearDown() {
    }

    @Test
    fun invoke() {
        assertTrue(linkGenerator.invoke(markdown).contains("1. [『武士道』感想](http://internal/『武士道』感想)"))
    }
}
/*
 * Copyright (c) 2023 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.licence.model

import io.mockk.MockKAnnotations
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.unmockkAll
import jp.toastkid.licence.model.text.Apache2
import jp.toastkid.licence.model.text.Bsd2
import jp.toastkid.licence.model.text.Mit
import org.junit.Assert.assertEquals

class LicensesLoaderTest {

    @InjectMockKs
    private lateinit var licensesLoader: LicensesLoader

    private val yml = """
- artifact: androidx.activity:activity-compose:+
  name: activity-compose
  copyrightHolder: The Android Open Source Project
  license: The Apache Software License, Version 2.0
  licenseUrl: https://opensource.org/licenses/Apache-2.0
  url: https://developer.android.com/jetpack/androidx/releases/activity#1.4.0
- artifact: com.github.ajalt.colormath:colormath-jvm:+
  name: colormath-jvm
  copyrightHolder: AJ Alt
  license: The MIT License
  licenseUrl: https://opensource.org/licenses/MIT
  url: https://github.com/ajalt/colormath
- artifact: org.commonmark:commonmark-ext-gfm-strikethrough:+
  name: commonmark-ext-gfm-strikethrough
  copyrightHolder: Robin Stocker
  license: BSD 2-Clause License
  licenseUrl: https://opensource.org/licenses/BSD-2-Clause 
      """.trimIndent()

    @org.junit.Before
    fun setUp() {
        MockKAnnotations.init(this)
    }

    @org.junit.After
    fun tearDown() {
        unmockkAll()
    }

    @org.junit.Test
    fun invoke() {
        val licenses = licensesLoader.invoke(yml.split("\n"))

        assertEquals(3, licenses.size)
        assertEquals(Apache2().text(), licenses[0].text)
        assertEquals("activity-compose", licenses[0].title)
        assertEquals(Mit().text(), licenses[1].text)
        assertEquals("colormath-jvm", licenses[1].title)
        assertEquals(Bsd2().text(), licenses[2].text)
        assertEquals("commonmark-ext-gfm-strikethrough", licenses[2].title)
    }

}
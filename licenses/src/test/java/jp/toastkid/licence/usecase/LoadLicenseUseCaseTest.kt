/*
 * Copyright (c) 2023 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.licence.usecase

import android.content.res.AssetManager
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.mockkConstructor
import io.mockk.unmockkAll
import io.mockk.verify
import jp.toastkid.licence.model.LicensesLoader
import java.io.ByteArrayInputStream

class LoadLicenseUseCaseTest {

    @InjectMockKs
    private lateinit var loadLicenseUseCase: LoadLicenseUseCase

    @MockK
    private lateinit var assets: AssetManager

    @org.junit.Before
    fun setUp() {
        MockKAnnotations.init(this)
        every { assets.open(any()) } returns ByteArrayInputStream(
            """
- artifact: androidx.activity:activity-compose:+
  name: activity-compose
  copyrightHolder: The Android Open Source Project
  license: The Apache Software License, Version 2.0
  licenseUrl: https://opensource.org/licenses/Apache-2.0
  url: https://developer.android.com/jetpack/androidx/releases/activity#1.4.0
            """.trimIndent()
                .toByteArray()
        )
        mockkConstructor(LicensesLoader::class)
        every { anyConstructed<LicensesLoader>().invoke(any()) } returns emptyList()
    }

    @org.junit.After
    fun tearDown() {
        unmockkAll()
    }

    @org.junit.Test
    fun invoke() {
        loadLicenseUseCase.invoke(assets)

        verify { assets.open(any()) }
        verify { anyConstructed<LicensesLoader>().invoke(any()) }
    }
}